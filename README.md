Android TrueColors Gradle Plugin
================================

Plugin to generate colors, dimens, strings and styles defined in a [TrueColors](https://github.com/vokal/TrueColors-OSX/blob/master/README.md) file.

# Setup

## If using Artifactory:
~~~gradle
buildscript {
    repositories {
        maven {
            url 'http://vokal-repo.ngrok.com/artifactory/repo'
            credentials {
                username = "${artifactory_user}"
                password = "${artifactory_password}"
            }
        }
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:${toolsVersion}'
        classpath 'io.vokal.gradle:truecolors:0.6.0'
    }
}
~~~

## If using precompiled JAR:
~~~gradle
buildscript {
    repositories {
        …
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:${toolsVersion}'
        classpath files('libs/truecolors-0.6.0.jar')
    }
}

~~~

# Usage

### Options

 * __useScalePixelDimens__ [_boolean_] - Default: true


~~~gradle
apply plugin: 'com.android.application'
apply plugin: 'io.vokal.truecolors'

android {
   …
}

truecolors {
   useScalePixelDimens false
}
~~~

Place TrueColors file in your source folder next to manifest file (ie. `/src/main/my.truecolors`).

It will copy the fonts to a `fonts` folder in the corresponding source set assets location and any dimension used in a font style will be set to scaled pixels (unless disabled with `useScalePixelDimens false`).
The styles have the `fontName` attribute which is the default used by [Calligraphy](https://github.com/chrisjenx/Calligraphy/blob/master/README.md#getting-started).
Setting up your Activity to wrap the base Context and using the styles in your xml layouts is all you should need to do by default.