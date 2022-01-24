package com.ssmmhh.jibam.utils.repeatTests

/**
 * how to use it?
 * add following rule to class:
 *      @get:Rule
 *      val repeatRule: RepeatRule = RepeatRule()
 * then annotate the desire test with " @Repeat(times = 10) " and " @Test "
 *
 * cons:
 * 1. @After and @Before test just run one time
 * 2. this annotation just repeat test statement so in run window it does not shows every test that
 * have been ran it just report one test
 * 3. cannot be used with classes only works with function
 *
 * source -> https://stackoverflow.com/a/28345802/10362460
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.ANNOTATION_CLASS,
)
annotation class Repeat(val times: Int = 1)
