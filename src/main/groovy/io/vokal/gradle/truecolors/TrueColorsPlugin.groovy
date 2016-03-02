package io.vokal.gradle.truecolors

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileTree

class TrueColorsPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.extensions.create('truecolors', TrueColorsExtension)

        project.afterEvaluate {
            def variants = null
            if (project.android.hasProperty('applicationVariants')) {
                variants = project.android.applicationVariants
            } else if (project.android.hasProperty('libraryVariants')) {
                variants = project.android.libraryVariants
            } else {
                return
            }

            def scalePixels = true
            if (project.truecolors != null && project.truecolors.hasProperty('useScalePixelDimens')) {
                scalePixels = project.truecolors.useScalePixelDimens
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
                        if (assetsDir == null)
                            assetsDir = src.assets.srcDirs[0]
                        if (tcSources == null) {
                            tcSources = tcCollection
                        } else {
                            tcSources.source(tcCollection)
                        }
                    }
                }

                def count = tcSources == null || tcSources.empty ? 0 : tcSources.files.size()

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
}
