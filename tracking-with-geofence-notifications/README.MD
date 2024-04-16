# Sample App: AWS Location Service Integration

This sample app demonstrates how to integrate AWS Location Service with an Android application using Amazon Cognito for authentication and tracking location updates in real time. It showcases the usage of Amazon Location Service's Tracking and Authentication SDKs to authenticate users and track their location on a map.

- **Tracking SDK**: [Amazon Location Service Tracking SDK](https://github.com/aws-geospatial/amazon-location-mobile-tracking-sdk-android)
- **Auth SDK**: [Amazon Location Service Authentication SDK](https://github.com/aws-geospatial/amazon-location-mobile-auth-sdk-android)

## Installation

1. Clone this repository to your local machine.
2. Open the `AmazonSampleSDKApp` project in Android Studio.
3. Build and run the app on your Android device or emulator.
4. Open `template.properties` file
5. Copy the content of `template.properties` file to a new file named `custom.properties`
6. Configure AWS credentials in the app using the following steps:
  - Step 1. **Configure custom.properties**: Fill in the required values in the `custom.properties` file to configure the AWS credentials. (Refer to the [Configuration](#configuration) section for details)
  - Step 2. **Configure inside app**: Upon launching the app, navigate to the Config tab and click on the Sign in button. This will open a dialogue in which you enter your AWS Identity Pool ID, Map Name, and Tracker Name to configure the AWS credentials.

## Configuration

Create a `custom.properties` file in your project directory and input the following lines, replacing placeholders with actual values:

 ```properties
 MQTT_END_POINT=xxxxxxxxxxxxx-xxx.xxx.us-east-1.amazonaws.com
 POLICY_NAME=xxxxxxxxx
 GEOFENCE_COLLECTION_NAME=xxxxxxxxxxxxxxxxx
 TOPIC_TRACKER=xxxxxxxxxx
```

- **MQTT_END_POINT**: The WebSocket URL in MQTT Amazon Location Service.
- **POLICY_NAME**: The name of the IoT policy in Amazon Location Service.
- **TOPIC_TRACKER**: The name of the topic in MQTT Amazon Location Service.
- **GEOFENCE_COLLECTION_NAME**: The Amazon Resource Name of your geofence collection in Amazon Location Service.

These values are used to configure the app's connection to AWS services.

## Filters

The app provides options to filter location tracking data based on time, distance, and accuracy:

- **Time Filter**: If enabled, location updates are sent only after the specified time interval has passed.
- **Distance Filter**: If enabled, location updates are sent only after moving a specified distance from the last reported location.
- **Accuracy Filter**: If enabled, location updates are filtered based on the specified accuracy level.

These filters help in optimizing the location tracking to suit different use cases and requirements.

## Automated UI test case

1. To run the Automated UI test case, add the below properties to the `custom.properties` file created above inside the Configuration section.

    ```properties
    DEFAULT_TRACKER_NAME=xxxxxxx
    TEST_POOL_ID=us-east-1:xxxxxxxxxxxxxxxxxxxxxxxxxx
    TEST_MAP_NAME=xxxxxxxxxxxxxxxxxxx
   ```

   - **DEFAULT_TRACKER_NAME**: The name of the tracker resource in Amazon Location Service used for tracking location updates.
   - **TEST_POOL_ID**: Your AWS Cognito Identity Pool ID used for authenticating users.
   - **TEST_MAP_NAME**: The name of the map resource in the Amazon Location Service you want to use to display the map.

2. Create and run the emulator of the latest Android version or plug in the real Android device.
3. Now open the `DefaultFlowSuite` file located inside the `androidTest` directory and click on the play button.

With the above steps, you can run the automated UI test cases.