pipeline {
    agent any

    triggers {
        // Poll SCM every 5 minutes
        pollSCM('H/5 * * * *')
    }

    environment {
        ANSIBLE_PLAYBOOK = 'deploy-playbook.yml'
        INVENTORY_FILE = 'inventory.ini'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                // Run tests using the SQLite test profile
                sh 'mvn clean test -Dspring.profiles.active=test'
                
                // Package the application
                sh 'mvn package -DskipTests'
            }
        }

        stage('Deploy to Web Server via Ansible') {
            steps {
                // Run the Ansible playbook to deploy the application
                sh "ansible-playbook -i ${INVENTORY_FILE} ${ANSIBLE_PLAYBOOK}"
            }
        }
    }

    post {
        failure {
            // Send email to srengty@gmail.com AND the developer who committed the error
            emailext (
                subject: "Build Failed: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
                body: """<p>The build or test phase has failed.</p>
                         <p>Please check the console output here: <a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a></p>""",
                to: "srengty@gmail.com",
                recipientProviders: [
                    culprits(), // Sends to the developers who made the commits
                    requestor() // Sends to the user who triggered the build
                ]
            )
        }
        success {
            echo "Successfully built, tested, and deployed via Ansible!"
        }
    }
}
