import base64
import os
import shutil
import subprocess
import sys

from jinja2 import Environment, FileSystemLoader
import pandas as pd

##Global variables
DOCKER_FILE_FOLDER = 'dockerfiles'
env, suts, sut_auths = None, None, None

class DockerGenerator:
    def __init__(self, sut_name, expose_port):
        self.SUT_POSTFIX = "-sut.jar"
        self.DOCKER_FILE_FOLDER = 'dockerfiles'

        self.sut_name = sut_name
        self.expose_port = expose_port
        self.jacoco_env_file_path = './data/.env'
        self.template_env = Environment(loader=FileSystemLoader("./templates"))
        self.suts = pd.read_csv('./data/sut.csv')
        self.auth_info = pd.read_csv('./data/auth_suts.csv')


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
            }
        ]

        if self.sut_name not in self.suts['NAME'].values:
            print(f"SUT {sut_name} not found in the SUT list")
            sys.exit(1)

        sut = self.suts.loc[self.suts['NAME'] == self.sut_name].iterrows().__next__()
        self.jdk_version = str(sut[1]['RUNTIME']) if str(sut[1]['RUNTIME']) != 'nan' else ''
        self.jvm_parameters = str(sut[1]['JVM_PARAMETERS']) if str(sut[1]['JVM_PARAMETERS']) != 'nan' else ''
        self.input_parameters = str(sut[1]['INPUT_PARAMETERS']) if str(sut[1]['INPUT_PARAMETERS']) != 'nan' else ''
        self.swagger_url = str(sut[1]['SWAGGER_URL']) if str(sut[1]['SWAGGER_URL']) != 'nan' else ''
        self.target_url = str(sut[1]['TARGET_URL']) if str(sut[1]['TARGET_URL']) != 'nan' else ''
        self.copy_additional_files = bool(sut[1]['COPY_ADDITIONAL_FILES'])

        auth_dict = self.get_auth_dict()

        self.prepare_generation()

    def prepare_generation(self):
        # prepare the required files
        if not os.path.exists(DOCKER_FILE_FOLDER):
            os.makedirs(DOCKER_FILE_FOLDER)
            shutil.copy(self.jacoco_env_file_path, DOCKER_FILE_FOLDER)

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
        files = []
        folder_name = f"./data/additional_files/{self.sut_name}"
        if self.copy_additional_files:
            file_list = os.listdir(folder_name)
            for file in file_list:
                files.append({
                    'source': f"{folder_name}/{file}",
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
            'EXPOSE_PORT': self.expose_port
        }

        template = self.template_env.get_template("template.docker-compose.yml")
        result = template.render(params)

        with open(f"{self.DOCKER_FILE_FOLDER}/{self.sut_name}.yml", "w") as f:
            f.write(result)

        print(f"Created {self.sut_name}.yml")


    def generate_em_yaml(self):
        params = {
            'bbSwaggerUrl': self.swagger_url,
            'bbTargetUrl': self.target_url,
            'auths': self.get_auth_dict()
        }

        template = self.template_env.get_template("template.em.yaml")

        result = template.render(params)

        with open(f"dockerfiles/{self.sut_name}.em.yaml", "w") as f:
            f.write(result)

        print(f"Created {self.sut_name}.em.yaml")

    def run_docker(self):
        # just for testing to see if the docker-compose file is working
        docker_file = f"./dockerfiles/{self.sut_name}.yml"
        subprocess.run(["docker-compose", "-f", docker_file, "up", "--build", "--abort-on-container-exit", "--remove-orphans"])

    def get_auth_dict(self):
        auth_suts = self.auth_info.loc[self.auth_info['sut_name'] == self.sut_name]
        auth_dicts = []
        for auth in auth_suts.iterrows():
            auth_name = auth[1].iloc[1]
            auth_type = auth[1].iloc[2]
            username = auth[1].iloc[3]
            password = auth[1].iloc[4]
            auth_prefix = auth[1].iloc[5]
            login_url = auth[1].iloc[6]
            header_name = auth[1].iloc[7]
            token = auth[1].iloc[8]
            auth_dict = {
                'auth_name': auth_name,
                'auth_type': auth_type,
                'username': username,
                'password': password,
                'auth_prefix': auth_prefix,
                'login_url': login_url,
                'header_name': header_name
            }
            if auth_type == 'basic':
                encoded_value = base64.b64encode(f"{username}:{password}".encode()).decode()
                header_value = f"{auth_prefix} {encoded_value}"
                auth_dict['header_value'] = header_value
            if auth_type == 'token':
                header_value = f"{auth_prefix} {token}"
                auth_dict['header_value'] = header_value
            auth_dicts.append(auth_dict)
        return auth_dicts

if __name__ == '__main__':
    # input parameters
    try:
        SUT_NAME = sys.argv[1]
    except IndexError:
        print("Please provide a SUT name")
        sys.exit(1)

    try:
        EXPOSE_PORT = int(sys.argv[2])
    except IndexError:
        # default port
        EXPOSE_PORT = 9090

    generator = DockerGenerator(SUT_NAME, EXPOSE_PORT)
    generator.generate_dockerfiles()
    generator.generate_docker_compose()
    # generator.generate_em_yaml() for authentication. No need to generate this file for now
    generator.run_docker()

