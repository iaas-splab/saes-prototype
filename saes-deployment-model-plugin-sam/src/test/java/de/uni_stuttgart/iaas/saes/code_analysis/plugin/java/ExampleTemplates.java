/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.code_analysis.plugin.java;

/**
 * contains sample definitions copied from
 * https://github.com/awsdocs/aws-lambda-developer-guide/blob/27683919cf7a39e4af634a32d30c2cf9d833e876/sample-apps
 */
public class ExampleTemplates {

	public static final String AWS_SAMPLE_APP_S3_JAVA = "AWSTemplateFormatVersion: '2010-09-09'\n" + 
			"Transform: 'AWS::Serverless-2016-10-31'\n" + 
			"Description: An AWS Lambda application that calls the Lambda API.\n" + 
			"Resources:\n" + 
			"  bucket:\n" + 
			"    Type: AWS::S3::Bucket\n" + 
			"    DeletionPolicy: Retain\n" + 
			"  function:\n" + 
			"    Type: AWS::Serverless::Function\n" + 
			"    Properties:\n" + 
			"      CodeUri: build/distributions/s3-java.zip\n" + 
			"      Handler: example.Handler\n" + 
			"      Runtime: java8\n" + 
			"      Description: Java function\n" + 
			"      MemorySize: 512\n" + 
			"      Timeout: 10\n" + 
			"      # Function's execution role\n" + 
			"      Policies:\n" + 
			"        - AWSLambdaBasicExecutionRole\n" + 
			"        - AWSLambdaReadOnlyAccess\n" + 
			"        - AWSXrayWriteOnlyAccess\n" + 
			"        - AWSLambdaVPCAccessExecutionRole\n" + 
			"        - AmazonS3FullAccess\n" + 
			"      Tracing: Active\n" + 
			"      Layers:\n" + 
			"        - !Ref libs\n" + 
			"      Events:\n" + 
			"        s3Notification:\n" + 
			"          Type: S3\n" + 
			"          Properties:\n" + 
			"            Bucket: !Ref bucket\n" + 
			"            Events: s3:ObjectCreated:*\n" + 
			"            Filter:\n" + 
			"              S3Key:\n" + 
			"                Rules:\n" + 
			"                - Name: prefix\n" + 
			"                  Value: inbound/\n" + 
			"  libs:\n" + 
			"    Type: AWS::Serverless::LayerVersion\n" + 
			"    Properties:\n" + 
			"      LayerName: s3-java-lib\n" + 
			"      Description: Dependencies for the Java S3 sample app.\n" + 
			"      ContentUri: build/s3-java-lib.zip\n" + 
			"      CompatibleRuntimes:\n" + 
			"        - java8"
	+ "";
	public static final String AWS_SAMPLE_APP_LIST_MANAGER = "AWSTemplateFormatVersion: 2010-09-09\n" + 
			"Description: An AWS Lambda application that uses Amazon Kinesis and Amazon RDS.\n" + 
			"Transform: AWS::Serverless-2016-10-31\n" + 
			"Parameters:\n" + 
			"  vpcStackName:\n" + 
			"    Default: list-manager-vpc\n" + 
			"    Description: VPC and database stack name\n" + 
			"    Type: String\n" + 
			"Globals:\n" + 
			"  Function:\n" + 
			"    Runtime: nodejs12.x\n" + 
			"    Tracing: Active\n" + 
			"    Handler: index.handler\n" + 
			"    AutoPublishAlias: live\n" + 
			"    Environment:\n" + 
			"        Variables:\n" + 
			"          table: !Ref table\n" + 
			"          aggtable: !Ref aggtable\n" + 
			"          databaseHost:\n" + 
			"            Fn::ImportValue:\n" + 
			"              !Sub \"${vpcStackName}-db-host\"\n" + 
			"          databaseName:\n" + 
			"            Fn::ImportValue:\n" + 
			"              !Sub \"${vpcStackName}-db-name\"\n" + 
			"          databaseUser:\n" + 
			"            Fn::ImportValue:\n" + 
			"              !Sub \"${vpcStackName}-db-user\"\n" + 
			"          databasePassword: '{{resolve:secretsmanager:list-manager:SecretString:password}}'\n" + 
			"    VpcConfig:\n" + 
			"      SecurityGroupIds:\n" + 
			"        - Fn::ImportValue:\n" + 
			"              !Sub \"${vpcStackName}-vpc-sg\"\n" + 
			"      SubnetIds:\n" + 
			"        - Fn::ImportValue:\n" + 
			"              !Sub \"${vpcStackName}-subnet-a\"\n" + 
			"        - Fn::ImportValue:\n" + 
			"              !Sub \"${vpcStackName}-subnet-b\"\n" + 
			"    DeploymentPreference:\n" + 
			"      Type: AllAtOnce\n" + 
			"      Role: !GetAtt deployrole.Arn\n" + 
			"    Layers:\n" + 
			"      - !Ref libs\n" + 
			"Resources:\n" + 
			"  deployrole:\n" + 
			"    Type: AWS::IAM::Role\n" + 
			"    Properties:\n" + 
			"      AssumeRolePolicyDocument:\n" + 
			"        Version: \"2012-10-17\"\n" + 
			"        Statement:\n" + 
			"          - Effect: Allow\n" + 
			"            Principal:\n" + 
			"              Service:\n" + 
			"                - codedeploy.amazonaws.com\n" + 
			"            Action:\n" + 
			"              - sts:AssumeRole\n" + 
			"      ManagedPolicyArns:\n" + 
			"        - arn:aws:iam::aws:policy/service-role/AWSCodeDeployRoleForLambda\n" + 
			"  libs:\n" + 
			"    Type: AWS::Serverless::LayerVersion\n" + 
			"    Properties:\n" + 
			"      LayerName: list-manager-lib\n" + 
			"      Description: Dependencies for the list manager sample app.\n" + 
			"      ContentUri: lib/.\n" + 
			"      CompatibleRuntimes:\n" + 
			"        - nodejs12.x\n" + 
			"  dbadmin:\n" + 
			"    Type: AWS::Serverless::Function\n" + 
			"    Properties:\n" + 
			"      CodeUri: dbadmin/.\n" + 
			"      Description: Run SQL queries.\n" + 
			"      MemorySize: 128\n" + 
			"      Timeout: 15\n" + 
			"      # Function's execution role\n" + 
			"      Policies:\n" + 
			"        - AWSLambdaBasicExecutionRole\n" + 
			"        - AWSLambdaVPCAccessExecutionRole\n" + 
			"  processor:\n" + 
			"    Type: AWS::Serverless::Function\n" + 
			"    Properties:\n" + 
			"      CodeUri: processor/.\n" + 
			"      Description: Process events from Amazon Kinesis\n" + 
			"      MemorySize: 128\n" + 
			"      Timeout: 100\n" + 
			"      # Function's execution role\n" + 
			"      Policies:\n" + 
			"        - AWSLambdaBasicExecutionRole\n" + 
			"        - AWSLambdaKinesisExecutionRole\n" + 
			"        - AmazonDynamoDBFullAccess\n" + 
			"        - AWSLambdaVPCAccessExecutionRole\n" + 
			"      Events:\n" + 
			"        kinesis:\n" + 
			"          Type: Kinesis\n" + 
			"          Properties:\n" + 
			"            Stream: !GetAtt stream.Arn\n" + 
			"            BatchSize: 100\n" + 
			"            StartingPosition: LATEST\n" + 
			"  stream:\n" + 
			"    Type: AWS::Kinesis::Stream\n" + 
			"    Properties:\n" + 
			"      ShardCount: 1\n" + 
			"  table:\n" + 
			"    Type: AWS::DynamoDB::Table\n" + 
			"    Properties:\n" + 
			"      AttributeDefinitions:\n" + 
			"        - AttributeName: \"id\"\n" + 
			"          AttributeType: \"S\"\n" + 
			"        - AttributeName: \"aggid\"\n" + 
			"          AttributeType: \"S\"\n" + 
			"      KeySchema:\n" + 
			"        - AttributeName: \"aggid\"\n" + 
			"          KeyType: \"HASH\"\n" + 
			"        - AttributeName: \"id\"\n" + 
			"          KeyType: \"RANGE\"\n" + 
			"      ProvisionedThroughput:\n" + 
			"        ReadCapacityUnits: \"2\"\n" + 
			"        WriteCapacityUnits: \"2\"\n" + 
			"  aggtable:\n" + 
			"    Type: AWS::Serverless::SimpleTable\n" + 
			"    Properties:\n" + 
			"      PrimaryKey:\n" + 
			"        Name: id\n" + 
			"        Type: String\n" + 
			"      ProvisionedThroughput:\n" + 
			"        ReadCapacityUnits: 2\n" + 
			"        WriteCapacityUnits: 2\n" + 
			"";
	public static final String AWS_SAMPLE_APP_ERROR_PROCESSOR = "AWSTemplateFormatVersion: '2010-09-09'\n" + 
			"Transform: 'AWS::Serverless-2016-10-31'\n" + 
			"Description: An AWS Lambda application that uses Amazon CloudWatch Logs, AWS X-Ray, and AWS CloudFormation custom resources.\n" + 
			"Globals:\n" + 
			"  Function:\n" + 
			"    Runtime: nodejs12.x\n" + 
			"    Handler: index.handler\n" + 
			"    Tracing: Active\n" + 
			"    Layers:\n" + 
			"      - !Ref libs\n" + 
			"Resources:\n" + 
			"  bucket:\n" + 
			"    Type: AWS::S3::Bucket\n" + 
			"    DeletionPolicy: Retain\n" + 
			"  role:\n" + 
			"    Type: AWS::IAM::Role\n" + 
			"    Properties:\n" + 
			"      AssumeRolePolicyDocument:\n" + 
			"        Version: \"2012-10-17\"\n" + 
			"        Statement:\n" + 
			"          -\n" + 
			"            Effect: Allow\n" + 
			"            Principal:\n" + 
			"              Service:\n" + 
			"                - lambda.amazonaws.com\n" + 
			"            Action:\n" + 
			"              - sts:AssumeRole\n" + 
			"      ManagedPolicyArns:\n" + 
			"        - arn:aws:iam::aws:policy/AWSXrayFullAccess\n" + 
			"        - arn:aws:iam::aws:policy/AmazonS3FullAccess\n" + 
			"        - arn:aws:iam::aws:policy/CloudWatchLogsFullAccess\n" + 
			"        - arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole\n" + 
			"        - arn:aws:iam::aws:policy/service-role/AWSLambdaRole\n" + 
			"      Path: /service-role/\n" + 
			"  processor:\n" + 
			"    Type: AWS::Serverless::Function\n" + 
			"    Properties:\n" + 
			"      CodeUri: processor/.\n" + 
			"      Description: Retrieve logs and trace for errors.\n" + 
			"      Timeout: 40\n" + 
			"      Role: !GetAtt role.Arn\n" + 
			"      Environment:\n" + 
			"        Variables:\n" + 
			"          bucket: !Ref bucket\n" + 
			"  libs:\n" + 
			"    Type: AWS::Serverless::LayerVersion\n" + 
			"    Properties:\n" + 
			"      LayerName: error-processor-lib\n" + 
			"      Description: Dependencies for the error-processor sample app.\n" + 
			"      ContentUri: lib/.\n" + 
			"      CompatibleRuntimes:\n" + 
			"        - nodejs12.x\n" + 
			"  randomerror:\n" + 
			"    Type: AWS::Serverless::Function\n" + 
			"    Properties:\n" + 
			"      CodeUri: random-error/.\n" + 
			"      Description: Generate errors.\n" + 
			"      Timeout: 600\n" + 
			"      Role: !GetAtt role.Arn\n" + 
			"  primer:\n" + 
			"    Type: AWS::Serverless::Function\n" + 
			"    Properties:\n" + 
			"      InlineCode: |\n" + 
			"        var aws = require('aws-sdk')\n" + 
			"        var response = require('cfn-response')\n" + 
			"        exports.handler = async function(event, context) {\n" + 
			"          console.log(\"REQUEST RECEIVED:\\n\" + JSON.stringify(event))\n" + 
			"          // For Delete requests, immediately send a SUCCESS response.\n" + 
			"          if (event.RequestType == \"Delete\") {\n" + 
			"            return await response.send(event, context, \"SUCCESS\")\n" + 
			"          }\n" + 
			"          var responseStatus = \"FAILED\"\n" + 
			"          var responseData = {}\n" + 
			"          var functionName1 = event.ResourceProperties.FunctionName1\n" + 
			"          var functionName2 = event.ResourceProperties.FunctionName2\n" + 
			"          var functionName3 = event.ResourceProperties.FunctionName3\n" + 
			"          var logGroup1 = \"/aws/lambda/\" + functionName1\n" + 
			"          var logGroup2 = \"/aws/lambda/\" + functionName2\n" + 
			"          var logGroup3 = \"/aws/lambda/\" + functionName3\n" + 
			"          var lambda = new aws.Lambda()\n" + 
			"          var logs = new aws.CloudWatchLogs()\n" + 
			"          try {\n" + 
			"            // Invoke other functions and wait for log groups to populate\n" + 
			"            await Promise.all([\n" + 
			"              lambda.invoke({ FunctionName: functionName2 }).promise(),\n" + 
			"              lambda.invoke({ FunctionName: functionName3 }).promise(),\n" + 
			"              new Promise(resolve => setTimeout(resolve, 10000))\n" + 
			"            ])\n" + 
			"            // Set log retention on all log groups\n" + 
			"            await Promise.all([\n" + 
			"              logs.putRetentionPolicy({logGroupName: logGroup1, retentionInDays: 3 }).promise(),\n" + 
			"              logs.putRetentionPolicy({logGroupName: logGroup2, retentionInDays: 3 }).promise(),\n" + 
			"              logs.putRetentionPolicy({logGroupName: logGroup3, retentionInDays: 3 }).promise()\n" + 
			"            ])} catch(err) {\n" + 
			"            responseData = {Error: \"SDK call failed\"}\n" + 
			"            console.log(responseData.Error + \":\\n\", err)\n" + 
			"            return await response.send(event, context, responseStatus, responseData)\n" + 
			"          }\n" + 
			"          responseStatus = \"SUCCESS\"\n" + 
			"          return await response.send(event, context, responseStatus, responseData)\n" + 
			"        }\n" + 
			"      Description: Invoke a function to create a log stream.\n" + 
			"      Role: !GetAtt role.Arn\n" + 
			"      Timeout: 30\n" + 
			"  primerinvoke:\n" + 
			"    Type: AWS::CloudFormation::CustomResource\n" + 
			"    Version: \"1.0\"\n" + 
			"    Properties:\n" + 
			"      ServiceToken: !GetAtt primer.Arn\n" + 
			"      FunctionName1: !Ref primer\n" + 
			"      FunctionName2: !Ref randomerror\n" + 
			"      FunctionName3: !Ref processor\n" + 
			"  subscription:\n" + 
			"    Type: AWS::Logs::SubscriptionFilter\n" + 
			"    DependsOn: cloudwatchlogspermission\n" + 
			"    Properties:\n" + 
			"      LogGroupName: !Join [ \"/\", [ \"/aws/lambda\", !Ref randomerror ] ]\n" + 
			"      FilterPattern: ERROR\n" + 
			"      DestinationArn: !GetAtt processor.Arn\n" + 
			"  cloudwatchlogspermission:\n" + 
			"    Type: AWS::Lambda::Permission\n" + 
			"    # Wait for randomerror to be invoked to ensure that the log stream exists.\n" + 
			"    DependsOn: primerinvoke\n" + 
			"    Properties:\n" + 
			"      FunctionName: !GetAtt processor.Arn\n" + 
			"      Action: lambda:InvokeFunction\n" + 
			"      Principal: !Join [ \".\", [ \"logs\", !Ref \"AWS::Region\", \"amazonaws.com\" ] ]\n" + 
			"      SourceAccount: !Ref AWS::AccountId\n" + 
			"";
}
