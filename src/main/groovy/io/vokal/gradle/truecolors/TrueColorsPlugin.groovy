package io.vokal.gradle.truecolors

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree

import java.nio.file.FileSystems

class TrueColorsPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.extensions.create('truecolors', TrueColorsExtension)

        project.afterEvaluate {

            dumpProperties(project.android)

            def variants = null
            if (project.android.hasProperty('applicationVariants')) {
                variants = project.android.applicationVariants
            } else if (project.android.hasProperty('libraryVariants')) {
                variants = project.android.libraryVariants
            } else {
                throw new IllegalStateException('Android project must have applicationVariants or libraryVariants!')
            }

            def scalePixels = true
            if (project.truecolors != null) {
                if (project.truecolors.hasProperty('useScalePixelDimens'))
                    scalePixels = project.truecolors.useScalePixelDimens
                else println "useScalePixelDimens defaultint to true"
            } else {
                println "using default TrueColors settings……"
            }

            def root = project.getProjectDir().getAbsolutePath()

            variants.all { variant ->
                def varNameCap = variant.name.capitalize()

                def assetsDir = null
                FileTree tcSources = null
                variant.sourceSets.each { src ->
                    def srcPath = "$root/src/$src.name"
                    def tcCollection = project.fileTree(srcPath) {
                        include "**/*.truecolors"
                    }
                    if (!tcCollection.empty) {
                        assetsDir = src.assets.srcDirs[0]
                        if (tcSources == null) {
                            tcSources = tcCollection
                        } else {
                            tcSources.source(tcCollection)
                        }
                    }
                }

                def count = tcSources == null || tcSources.empty ? 0 : tcSources.files.size()
                println "[$count] TrueColors ($varNameCap)"
                if (tcSources != null) dumpProperties(tcSources)
                println()

                if (count > 0) {
                    String outDir = "$project.buildDir/generated/res/truecolors/$variant.dirName/"

                    def trueTaskName = "generate${varNameCap}ResTrueColors"
                    Task trueTask = project.task(trueTaskName, type: TrueColorsTask) {
                        sources = tcSources
                        outputDir = project.file(outDir)
                        assetFolder = project.file(assetsDir)
                        useScalePixels = scalePixels
                    }

                    // we would do this but Android Studio to see the generated resources
                    // (maybe it will be fixed in the future, does not work as of 2.0-beta6)
//                  variant.registerResGeneratingTask(trueTask, trueTask.outputDir)

                    // so, we register dependency directly and add outputs to most specific source set
                    project.tasks["generate${varNameCap}Resources"].dependsOn(trueTaskName)
                    variant.sourceSets[variant.sourceSets.size() - 1].res.srcDirs += outDir
                }
            }
        }
    }

    public static String wildcardToRegex(String wildcard) {
        StringBuffer s = new StringBuffer(wildcard.length())
        s.append('^')
        int len = wildcard.length()
        for (int i = 0; i < len; i++) {
            char c = wildcard.charAt(i)
            switch (c) {
                case '*':
                    s.append(".*")
                    break
                case '?':
                    s.append(".")
                    break
            // escape special regexp-characters
                case '(': case ')': case '[': case ']': case '$':
                case '^': case '.': case '{': case '}': case '|':
                case '\\':
                    s.append("\\")
                    s.append(c)
                    break
                default:
                    s.append(c)
                    break
            }
        }
        s.append('$')
        return (s.toString())
    }

    public static void dumpProperties(Object object) {
        def filtered = ['class', 'active']
        if (object == null) {
            print "Object == NULL"
            return
        }
        println "${object.class.canonicalName}: $object"
        println object.properties
                .sort { it.key }
                .collect { it }
                .findAll { !filtered.contains(it.key) }
                .collect {
            def t = "?"
            if (it.value == null) t = "null"
            else if (it.value.class == null) t = "primitive"
            else t = it.value.class.simpleName
            "$t: $it"
        }.join('\n')
        println()
    }

    static {
        System.setProperty("java.awt.headless", "true")

        // workaround for an Android Studio issue
        try {
            Class.forName(System.getProperty("java.awt.graphicsenv"))
        } catch (ClassNotFoundException e) {
            System.err.println("[WARN] java.awt.graphicsenv: " + e)
            System.setProperty("java.awt.graphicsenv", "sun.awt.CGraphicsEnvironment")
        }
        try {
            Class.forName(System.getProperty("awt.toolkit"))
        } catch (ClassNotFoundException e) {
            System.err.println("[WARN] awt.toolkit: " + e)
            System.setProperty("awt.toolkit", "sun.lwawt.macosx.LWCToolkit")
        }
    }
}
