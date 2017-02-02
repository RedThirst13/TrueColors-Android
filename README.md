Android TrueColors Gradle Plugin
================================

Plugin to generate colors, dimens, strings and styles defined in a [TrueColors](https://github.com/vokal/TrueColors-macOS/blob/master/README.md) file.

# Usage
~~~gradle
buildscript {
    dependencies {
        classpath 'io.vokal.gradle:truecolors:1.0.0'
    }
}

apply plugin: 'io.vokal.truecolors'

truecolors {
   useScalePixelDimens false
}

dependencies {
    compile 'uk.co.chrisjenx:calligraphy:2.1.0'
}
~~~


Place your TrueColors file in your source folder next to manifest file (ie. `/src/main/my.truecolors`).

The font styles have the `fontName` attribute which is the default used by [Calligraphy](https://github.com/chrisjenx/Calligraphy/blob/master/README.md#getting-started).
Setting up your Activity to wrap the base Context and using the styles in your xml layouts is all you should need to do by default.


### Options
 * __useScalePixelDimens__ [_boolean_] - dimension used in any font styles will use scaled pixels ("25sp"). Default: true