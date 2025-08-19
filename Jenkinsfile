pipeline {
    agent any

    environment {
        MAVEN_OPTS = '-Dmaven.test.skip=false'
    }

    stages {
        stage('Checkout Code') {
            steps {
                git credentialsId: 'partiedev',
                    branch: 'main',  // adapte si ce n'est pas main
                    url: 'https://github.com/nadra-wertani/habilitationsbna.git'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }

        stage('Run Tests') {
            steps {
                sh 'mvn test'
            }
        }

        stage('JaCoCo Report Mockito') {
            steps {
                sh 'mvn jacoco:report'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                    sh 'mvn sonar:sonar -Dsonar.login=$SONAR_TOKEN'
                }
            }
        }

        stage('MVN Nexus') {
            steps {
                sh 'mvn deploy -Dmaven.test.skip=true'
            }
        }

        stage('Grafana') {
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
