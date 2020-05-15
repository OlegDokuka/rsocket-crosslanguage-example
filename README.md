# RSocket Cross Language Example
 
## Requirements

1. JDK 11+
1. NodeJS 10+

## Installation

To install JS Side, please enter the following commands in the terminal

```shell script
cd ./js-client
npm install
```

The above command should install the required RSocket-JS dependencies

To build Java side, please type the following command in the project-root directory

```shell script
./gradlew clean assemble
```

## Running examples

In order to run the browser client, please type in the following command

```shell script
cd ./js-client
npm run start:dev
```

This command bootstrap a webpack-dev-server that will be hosting a webpage on the `http://localhost:9001/`.

To run Java server, please input the following terminal command

```shell script
./gradlew bootRun
```

This command bootstrap an RSocket-Server on the `ws://localhost:8080`.

## Vanilla RSocket-Java & RSocket-JS

To play around with a vanilla RSocket, please checkout the first commit int this
 repository.
 
## Spring RSocket & RSocket-JS

To play with Spring RSocket and RSocket-JS, please checkout the latest commit



