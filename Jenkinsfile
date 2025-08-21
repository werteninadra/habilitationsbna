pipeline {
    agent any

    environment {
        MAVEN_OPTS = '-Dmaven.test.skip=false'
    }

    stages {
        stage('Checkout Code') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/nadra-wertani/habilitationsbna.git'
            }
        }

        stage('Build Backend') {
            steps {
                dir('habilitationbna') {  // on cible uniquement le back
                    sh 'mvn clean compile'
                }
            }
        }

        stage('Run Tests Backend') {
            steps {
                dir('habilitationbna') {
                    sh 'mvn test'
                }
            }
        }
        stage('SonarQube Analysis') {
            environment {
                SONAR_HOST_URL = 'http://localhost:9000'
                SONAR_LOGIN = credentials('sonar-token') // 🔑 doit exister dans Jenkins
            }
            steps {
                dir('habilitationbna') {
                    sh '''
                        mvn sonar:sonar \
                          -Dsonar.projectKey=habilitationbna \
                          -Dsonar.projectName="habilitationbna" \
                          -Dsonar.host.url=$SONAR_HOST_URL \
                          -Dsonar.login=$SONAR_LOGIN
                    '''
                }
            }
        }
    }

        /*
        stage('JaCoCo Report') {
            steps {
                dir('habilitationbna') {
                    sh 'mvn jacoco:report'
                }
            }
        }

        stage('SonarQube Analysis') {
            environment {
                SONAR_HOST_URL = 'http://localhost:9000'
                SONAR_LOGIN = credentials('sonar-token') // 🔑 doit exister dans Jenkins
            }
            steps {
                dir('habilitationbna') {
                    sh 'mvn sonar:sonar -Dsonar.host.url=$SONAR_HOST_URL -Dsonar.login=$SONAR_LOGIN'
                }
            }
        }

        stage('Deploy to Nexus') {
            steps {
                dir('habilitationbna') {
                    sh 'mvn deploy -Dmaven.test.skip=true'
                }
            }
        }

        stage('Push Metrics to Prometheus/Grafana') {
            steps {
                script {
                    def status = currentBuild.currentResult == 'SUCCESS' ? 1 : 0
                    sh """
                        cat <<EOF | curl --data-binary @- http://localhost:9090/metrics/job/${env.JOB_NAME}/build/${env.BUILD_NUMBER}
                        # TYPE jenkins_build_status gauge
                        jenkins_build_status{job="${env.JOB_NAME}",build="${env.BUILD_NUMBER}"} ${status}
                        EOF
                    """
                }
            }
        }
        */
    }

    post {
        success {
            mail to: 'werteninadra@gmail.com',
                 subject: "✅ Pipeline réussie : ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: "La pipeline ${env.JOB_NAME} #${env.BUILD_NUMBER} a été exécutée avec succès.\nConsultez les logs : ${env.BUILD_URL}"
            echo 'Pipeline exécutée avec succès'
        }
        failure {
            mail to: 'werteninadra@gmail.com',
                 subject: "❌ Échec de la pipeline : ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: "La pipeline ${env.JOB_NAME} #${env.BUILD_NUMBER} a échoué.\nConsultez les logs : ${env.BUILD_URL}"
            echo 'La pipeline a échoué'
        }
    }
}
