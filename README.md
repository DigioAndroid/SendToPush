# SendToPush

Android Library for SendToPush service (http://www.sendtopush.com/)

## Integration

Grab via Maven:
```xml
<dependency>
  <groupId>com.github</groupId>
  <artifactId>sendtopush</artifactId>
  <version>2.0.2</version>
</dependency>
```
or Gradle:
```groovy
repositories {
    mavenCentral()
}

compile 'com.github.DigioAndroid:sendtopush:2.0.2'
```

## Usage

See sample project for details.

### Firebase configuration

build.gradle raiz del proyecto:

```` buildscript {
  dependencies {
    // Add this line
    classpath 'com.google.gms:google-services:4.0.0'
  }
}
````

Incluir plugin de google play services en build.gradle del modulo app:

````
apply plugin: 'com.google.gms.google-services' //Añadir al final del fichero
````

**Importante: en el Manifest de la app cliente no hay que añadir nada relacionado con FCM, está incluido en la librería. Si el proyecto contiene alguna referencia en relación a receivers de GCM hay que eliminarla.**


Si se necesita extender la clase PushReceiver para procesar la notificación de forma manual, declarar en el manifest del cliente:

````
 <service
            android:name="my.package.CustomPushReceiver">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>
        ````


Incluir fichero google-services.json en la carpeta app del proyecto, obtenido desde la consola de firebase.

#### Registration

    SendToPushManager.init(APIKEY, COMPANY, APPNAME, GCM_SENDER_ID, BuildConfig.DEBUG ? Environment.SANDBOX : Environment.PRODUCTION);
    SendToPushManager.getInstance().register(this, "sampleUser", null);
    
#### Unregistration

     SendToPushManager.getInstance().unregister(this);
     
#### Receive notifications

    public class PushReceiver extends com.accesium.sendtopush.PushReceiver {
   
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Broadcast received");
    }

    protected void handleMessage(Context context, Intent intent) {
        Log.d("Extras: " + intent.getExtras().toString());
    }
}


You will need to add some permisions to your manifest, services and receivers. See wiki for details.


## Documentation

* [Configure Manifest](https://github.com/DigioAndroid/SendToPush/wiki/Configure-Manifest)
* [Class diagram](https://github.com/DigioAndroid/SendToPush/wiki/Class-diagram)
* [PushError](https://github.com/DigioAndroid/SendToPush/wiki/PushError)
* [PushMessage](https://github.com/DigioAndroid/SendToPush/wiki/PushMessage)
* [PushStateType](https://github.com/DigioAndroid/SendToPush/wiki/PushStateType)
* [SendToPushManager](https://github.com/DigioAndroid/SendToPush/wiki/SendToPushManager)
* [Type](https://github.com/DigioAndroid/SendToPush/wiki/Type)

## License

[Apache Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

    Copyright (C) 2015 DIGIO SOLUCIONES DIGITALES

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


## Author

DIGIO SOLUCIONES DIGITALES  | http://www.digio.es/home
