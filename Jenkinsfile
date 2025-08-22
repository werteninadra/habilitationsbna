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
                dir('habilitationbna') {
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

        stage('JaCoCo Coverage') {
            steps {
                dir('habilitationbna') {
                    // Génération du rapport JaCoCo
                    sh 'mvn jacoco:report'
                }
                // Publier le rapport JaCoCo dans Jenkins
                jacoco execPattern: '**/target/jacoco.exec', classPattern: '**/target/classes', sourcePattern: '**/src/main/java'
            }
        }

        stage('SonarQube Analysis') {
            environment {
                SONAR_HOST_URL = 'http://localhost:9000'
                SONAR_LOGIN = credentials('sonar-token')
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

        /*
        stage('Deploy to Nexus') {
            steps {
                dir('habilitationbna') {
                    sh 'mvn deploy -Dmaven.test.skip=true'
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
