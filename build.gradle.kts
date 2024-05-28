/*
 * Copyright (c) 2023 Adevinta
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.lint) apply false
    alias(libs.plugins.android.kotlin) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.google.ksp) apply false
    alias(libs.plugins.paparazzi) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.dependencyGuard) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.compose) apply false

    id("com.adevinta.spark.root")
    id("com.adevinta.spark.dokka")
}

allprojects {
    apply(plugin = "com.adevinta.spark.spotless")
}

// https://github.com/google/guava/issues/6801
libs.versions.paparazzi.get().let {
    if (it != "1.3.3") {
        throw GradleException(
            """
            It seems like you've just updated Paparazzi to $it.
            Please check if this version includes a fix for Guava's -jre published variant.
            If this is the case, please remove this check and the workaround below, otherwise, update the version check above.
            """.trimIndent(),
        )
    }
    subprojects {
        plugins.withId("app.cash.paparazzi") {
            // Defer until afterEvaluate so that testImplementation is created by Android plugin.
            afterEvaluate {
                dependencies.constraints {
                    add("testImplementation", "com.google.guava:guava") {
                        attributes {
                            attribute(
                                TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE,
                                objects.named(TargetJvmEnvironment.STANDARD_JVM),
                            )
                        }
                        because("LayoutLib and sdk-common depend on Guava's -jre published variant.")
                    }
                }
            }
        }
    }
}
