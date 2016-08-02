![alt tag](app/src/main/res/mipmap-xxxhdpi/ic_launcher.png)

# Bittypic
#### An itty bitty photo sharing app, built on a 5-hour train ride to AnDevCon Boston

This is a proof of concept app to show how a streamlined, mobile-backend-as-a-service approach to app development empowers Android developers to make anything. Bittypic simply connects people with their friends so they can share photos. 

### Features Covered
- Social login
- User profiles
- User-generated content
- File storage
- Real-time push data
- Push notifications

This app makes heavy use of Firebase and OneSignal to support its feature set. 

### Getting Started

- Create a new [Firebase project](https://firebase.google.com/)
- Create a new [Facebook app](https://developers.facebook.com/)
- Create a new [OneSignal app](https://onesignal.com/)
- Generate a [google-services.json](https://developers.google.com/mobile/add?platform=android&cntapi=analytics&cntapp=Default%20Demo%20App&cntpkg=com.google.samples.quickstart.analytics&cnturl=https:%2F%2Fdevelopers.google.com%2Fanalytics%2Fdevguides%2Fcollection%2Fandroid%2Fv4%2Fstart%3Fconfigured%3Dtrue&cntlbl=Continue%20with%20Try%20Analytics) file and add it to your app directory

#### Add the following configuration variables to your gradle.properties
- FACEBOOK_APP_ID
- FB_LOGIN_PROTOCOL_SCHEME
- FIREBASE_BUCKET
- ONESIGNAL_APP_ID
- ONESIGNAL_GOOGLE_PROJECT_NUMBER



