## Java CLI app

### Build instructions
* docker build -t java-cli-app .

### Run instructions:
* create command (examples)
  * without parent id: 
    * docker run --rm java-cli-app create "Example issue"
  * with parent id
    * docker run --rm java-cli-app create "Example issue" 1


* update command (example)
  * docker run --rm java-cli-app update 1 IN_PROGRESS


* list command (example)
  * docker run --rm java-cli-app list CLOSED

