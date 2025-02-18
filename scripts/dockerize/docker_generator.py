import os
import shutil
import subprocess
import sys

from jinja2 import Environment, FileSystemLoader
import pandas as pd


class DockerGenerator:
    def __init__(self, sut_name, expose_port):
        self.SUT_POSTFIX = "-sut.jar"
        self.DOCKER_FILE_FOLDER = '../dockerfiles'

        self.sut_name = sut_name
        self.expose_port = expose_port
        self.jacoco_env_file_path = './data/.env'
        self.template_env = Environment(loader=FileSystemLoader("./dockerize/templates"))
        self.suts = pd.read_csv('./dockerize/data/sut.csv')
        self.sut_info = pd.read_csv('../statistics/data.csv')

        if not os.path.exists(self.DOCKER_FILE_FOLDER):
            os.makedirs(self.DOCKER_FILE_FOLDER)

        self.BASE_IMAGES = [
            {
                'name': 'JDK 8',
                'tag': 'amazoncorretto:8-alpine-jdk',
            },
            {
                'name': 'JDK 11',
                'tag': 'amazoncorretto:11-alpine-jdk',
            },
            {
                'name': 'JDK 17',
                'tag': 'amazoncorretto:17-alpine-jdk',
            },
            {
                'name': 'JDK 21',
                'tag': 'amazoncorretto:21-alpine-jdk',
            }
        ]

        if self.sut_name not in self.sut_info['NAME'].values or self.sut_name not in self.suts['NAME'].values:
            print(f"SUT {sut_name} not found in the SUT list")
            sys.exit(1)

        sut = self.suts.loc[self.suts['NAME'] == self.sut_name].iterrows().__next__()
        sut_info = self.sut_info.loc[self.sut_info['NAME'] == self.sut_name].iterrows().__next__()

        self.jdk_version = str(sut_info[1]['RUNTIME']) if str(sut_info[1]['RUNTIME']) != 'nan' else ''
        self.jvm_parameters = str(sut[1]['JVM_PARAMETERS']) if str(sut[1]['JVM_PARAMETERS']) != 'nan' else ''
        self.input_parameters = str(sut[1]['INPUT_PARAMETERS']) if str(sut[1]['INPUT_PARAMETERS']) != 'nan' else ''
        self.swagger_url = str(sut[1]['SWAGGER_URL']) if str(sut[1]['SWAGGER_URL']) != 'nan' else ''
        self.target_url = str(sut[1]['TARGET_URL']) if str(sut[1]['TARGET_URL']) != 'nan' else ''
        self.copy_additional_files = bool(sut[1]['COPY_ADDITIONAL_FILES'])
        self.database_types = str(sut_info[1]['DATABASE']).split(';') if str(sut_info[1]['DATABASE']) != 'nan' else None
        self.database_image = str(sut[1]['DATABASE_IMAGE_NAME']) if str(sut[1]['DATABASE_IMAGE_NAME']) != 'nan' else ''
        self.database_port = int(sut[1]['DATABASE_PORT']) if str(sut[1]['DATABASE_PORT']) != 'nan' else ''
        self.tmp_fs = str(sut[1]['TMP_FS']) if str(sut[1]['TMP_FS']) != 'nan' else ''
        self.database_environments = str(sut[1]['DATABASE_ENVIRONMENT']).split(';') if str(sut[1]['DATABASE_ENVIRONMENT']) != 'nan' else None
        self.database_volumes = str(sut[1]['DATABASE_VOLUME']).split(';') if str(sut[1]['DATABASE_VOLUME']) != 'nan' else None
        self.is_mock_oauth = bool(sut[1]['MOCK_OAUTH'])
        self.health_check = bool(sut[1]['HEALTH_CHECK'])
        self.health_check_commands = str(sut[1]['HEALTH_CHECK_COMMAND']).split(';') if str(sut[1]['HEALTH_CHECK_COMMAND']) != 'nan' else ''

    def prepare_run_docker(self):
        # prepare the required files
        shutil.copy(self.jacoco_env_file_path, self.DOCKER_FILE_FOLDER)

        if not os.path.exists("dist"):
            os.makedirs("dist")

        # Copy the required jar files located in EMB/dist folder. First you need to compile the SUTs.
        # We are copying these files because of dockerfile restrictions. We cannot copy files from parent directory.
        sut_filename = f"{self.sut_name}{self.SUT_POSTFIX}"
        if not os.path.exists(f"./dist/{sut_filename}"):
            copy_file_name = f"../../dist/{sut_filename}"
            shutil.copy(copy_file_name, "./dist")

        if not os.path.exists(f"./dist/jacocoagent.jar"):
            shutil.copy(f"../../dist/jacocoagent.jar", "./dist")
        if not os.path.exists(f"./dist/jacococli.jar"):
            shutil.copy(f"../../dist/jacococli.jar", "./dist")

    def get_base_image(self, jdk_version):
        base_image = None
        for image in self.BASE_IMAGES:
            if image['name'] == jdk_version:
                base_image = image['tag']
                break
        return base_image


    def generate_dockerfiles(self):
        base_image = self.get_base_image(self.jdk_version)
        save_folder_path = f"./scripts/dockerize/data/additional_files/{self.sut_name}"
        files = []
        folder_name = f"./dockerize/data/additional_files/{self.sut_name}"
        if self.copy_additional_files:
            file_list = os.listdir(folder_name)
            for file in file_list:
                files.append({
                    'source': f"{save_folder_path}/{file}",
                })

        params = {
            'BASE_IMAGE': base_image,
            'SUT_NAME': self.sut_name,
            'EMB_DIR': "./dist",
            'JVM_PARAMETERS': self.jvm_parameters,
            'INPUT_PARAMETERS': self.input_parameters,
            'ADDITIONAL_FILES': self.copy_additional_files,
            'files': files
        }

        # Create a template object from a file
        template = self.template_env.get_template("template.dockerfile")

        result = template.render(params)

        with open(f"{self.DOCKER_FILE_FOLDER}/{self.sut_name}.dockerfile", "w") as f:
            f.write(result)

        print(f"Created {self.sut_name}.dockerfile")


    def generate_docker_compose(self):
        params = {
            'SUT_NAME': self.sut_name,
            'EXPOSE_PORT': self.expose_port,
            'MOCK_OAUTH': self.is_mock_oauth,
            'HEALTH_CHECK': self.health_check,
        }

        if self.database_types and any(db in ['PostgreSQL', 'MySQL', 'Redis', 'MongoDB'] for db in self.database_types):
            database_template = self.template_env.get_template("db.template")
            if self.health_check:
                health_chec_command = ",".join(self.health_check_commands)
            else:
                health_chec_command = ""
            database_params = {
                'DATABASE_IMAGE_NAME': self.database_image,
                'DATABASE_PORT': self.database_port,
                'TMP_FS': self.tmp_fs,
                'DATABASE_ENVIRONMENT': self.database_environments,
                'DATABASE_VOLUME': self.database_volumes,
                'HEALTH_CHECK': self.health_check,
                'HEALTH_CHECK_COMMAND': health_chec_command
            }
            database_image = database_template.render(database_params)
            params['MONGODB_DATABASE'] = database_image


        template = self.template_env.get_template("template.docker-compose.yml")
        result = template.render(params)

        with open(f"{self.DOCKER_FILE_FOLDER}/{self.sut_name}.yml", "w") as f:
            f.write(result)

        print(f"Created {self.sut_name}.yml")

    def run_docker(self):
        # just for testing to see if the docker-compose file is working
        self.prepare_run_docker()
        docker_file = f"./dockerfiles/{self.sut_name}.yml"
        subprocess.run(["docker-compose", "-f", docker_file, "up", "--build", "--abort-on-container-exit", "--remove-orphans"])


if __name__ == '__main__':
    if len(sys.argv) < 3:
        print("Usage:\ndocker_generator.py <sut_name> <expose_port> <run_on_docker>?")
        sys.exit(1)

    try:
        SUT_NAME = sys.argv[1]
    except IndexError:
        print("Please provide a SUT name")
        sys.exit(1)

    try:
        EXPOSE_PORT = int(sys.argv[2])
    except IndexError:
        # default port
        EXPOSE_PORT = 8080

    try:
        RUN_ON_DOCKER = bool(sys.argv[3] == "True")
    except IndexError:
        RUN_ON_DOCKER = False

    generator = DockerGenerator(SUT_NAME, EXPOSE_PORT)
    generator.generate_dockerfiles()
    generator.generate_docker_compose()

    if RUN_ON_DOCKER:
        generator.run_docker()
