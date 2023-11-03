pipeline {
    agent {
        docker {
            registryUrl 'https://us-docker.pkg.dev'
            image 'verdant-bulwark-278/bzm-plugin-base-image/bzm-plugin-base-image:latest'
            registryCredentialsId 'push-to-gar-enc'
            args '-u root -v /var/run/docker.sock:/var/run/docker.sock -v $WORKSPACE:/build'
        }
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: "10"))
        ansiColor('xterm')
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        API_TOKEN = credentials('atlassian-bamboo-api-key')
    }

    stages {
        stage('Build Artifacts') {
            steps {
                script {
                    sh'atlas-mvn clean install'
                }
            }
        }
        stage('Create Release') {
            steps {
                script {
                    sh 'mvn clean install'
                }
            }
        }
        stage('Upload Artifact') {
            steps {
                script {
                    sh 'curl --request POST --url https://marketplace.atlassian.com/rest/2/assets/artifact --user 'sat@blazemeter.com:${API_TOKEN}' \ --header 'Accept: application/json' \ --header 'Content-Type: multipart/form-data' \ --form file=@"my-file.jar"'
                }
            }
        }
    }
}
