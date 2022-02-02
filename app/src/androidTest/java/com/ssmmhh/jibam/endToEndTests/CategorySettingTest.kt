package com.ssmmhh.jibam.endToEndTests

import android.content.SharedPreferences
import android.content.res.Resources
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.models.Category
import com.ssmmhh.jibam.persistence.CategoriesDao
import com.ssmmhh.jibam.ui.main.MainActivity
import com.ssmmhh.jibam.util.Constants
import com.ssmmhh.jibam.util.EspressoIdlingResources
import com.ssmmhh.jibam.util.PreferenceKeys
import com.ssmmhh.jibam.utils.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * test category setting page
 */
@ExperimentalCoroutinesApi
@FlowPreview
@RunWith(AndroidJUnit4ClassRunner::class)
class CategorySettingTest {

    @Inject
    lateinit var sharedPrefEditor: SharedPreferences.Editor

    @Inject
    lateinit var resources: Resources

    @Inject
    lateinit var categoriesDao: CategoriesDao

    init {
        //inject this class using dagger
        getTestBaseApplication().mainComponent()
            .inject(this)
    }

    //a global variable to store mainActivity scenario instance to run before each test and close
    //after each test, did not use activityRule b/c APP_INTRO_PREFERENCE should set to false before
    //activity launch
    private var mainActivityScenario: ActivityScenario<MainActivity>? = null

    @Before
    fun beforeEach() {
        //register idling resources
        IdlingRegistry.getInstance().register(EspressoIdlingResources.countingIdlingResource)
        disableAllPromoteBanners(sharedPrefEditor)
        //set APP_INTRO_PREFERENCE to false, so it means user has seen the appIntro and click done
        //therefore mainActivity does not switch to AppIntroActivity
        sharedPrefEditor.putBoolean(PreferenceKeys.APP_INTRO_PREFERENCE, false).commit()
        //launch mainActivity
        mainActivityScenario = ActivityScenario.launch(MainActivity::class.java)

    }

    @After
    fun afterEach() {
        //unregister idling resources
        IdlingRegistry.getInstance().unregister(EspressoIdlingResources.countingIdlingResource)
        mainActivityScenario?.close()
        mainActivityScenario = null
    }

    @Test
    fun navigateToCategorySettingFragment_justCheckForTextValues() {

        //navigate to category setting fragment
        //open up the drawer menu
        onView(withContentDescription(R.string.navigation_drawer_cd))
            .perform(click())
        //click on about us item in menu
        onView(withId(R.id.viewCategoriesFragment)).perform(click())

        //confirm views texts

        //confirm toolbar title text
        onView(withId(R.id.topAppBar_normal)).check(
            matches(
                hasDescendant(
                    withText(
                        resources.getString(
                            R.string.category_setting
                        )
                    )
                )
            )
        )
        //confirm TabLayout text values
        onView(withId(R.id.tab_layout)).check(
            matches(
                tabLayoutWithTextAtPosition(
                    0,
                    resources.getString(R.string.expenses)
                )
            )
        )
        onView(withId(R.id.tab_layout)).check(
            matches(
                tabLayoutWithTextAtPosition(
                    1,
                    resources.getString(R.string.income)
                )
            )
        )
        //confirm add new category button title
        onView(withId(R.id.txt_addNewCategory)).check(matches(withText(R.string.add_new_category)))

    }

    @Test
    fun categorySetting_swipeLeftAndRightOnViewPager_confirmCategoryTypesChanges(): Unit =
        runBlocking {
            //navigate to category setting fragment
            //open up the drawer menu
            onView(withContentDescription(R.string.navigation_drawer_cd))
                .perform(click())
            //click on about us item in menu
            onView(withId(R.id.viewCategoriesFragment)).perform(click())
            //confirm recyclerViewValue
            onView(
                withId(R.id.recycler_viewCategories),
            ).check(
                matches(
                    atPositionOnView(
                        0,
                        withText("food"),//hardcoded name of category with order 0
                        R.id.nameOfCategory
                    )
                )
            )
            //swipe left then confirm next
            onView(withId(R.id.viewPager_viewCategories)).perform(customSwipeLeft())

            onView(
                withId(R.id.recycler_viewCategories),
            ).waitTillViewIsDisplayed(visibleAreaPercentage = 1).check(
                matches(
                    atPositionOnView(
                        0,
                        withText("salary"),//hardcoded name of category with order 0
                        R.id.nameOfCategory
                    )
                )
            )

        }

    @Test
    fun shouldContainRightTextValues_whenNavigateToAddExpensesCategory() {
        //navigate to category setting fragment
        //open up the drawer menu
        onView(withContentDescription(R.string.navigation_drawer_cd))
            .perform(click())
        //click on about us item in menu
        onView(withId(R.id.viewCategoriesFragment)).perform(click())

        //click on add new button to navigate to addCategoryFragment
        onView(withId(R.id.add_new_appbar)).perform(click())
        //Assertions
        //confirm toolbar title text
        onView(withId(R.id.topAppBar_normal)).check(
            matches(
                hasDescendant(
                    withText(
                        resources.getString(
                            R.string.add_expenses_category
                        )
                    )
                )
            )
        )
        //make sure editText is empty
        onView(withId(R.id.edt_categoryName)).check(matches(isDisplayed()))
            .check(matches(withText("")))
    }

    @Test
    fun shouldContainRightTextValues_whenNavigateToAddIncomeCategory(): Unit = runBlocking {
        //navigate to category setting fragment
        //open up the drawer menu
        onView(withContentDescription(R.string.navigation_drawer_cd))
            .perform(click())
        //click on about us item in menu
        onView(withId(R.id.viewCategoriesFragment)).perform(click())

        //swipe to income page of viewPager
        onView(withId(R.id.viewPager_viewCategories)).perform(customSwipeLeft())
        //click on add new button to navigate to addCategoryFragment
        onView(withId(R.id.add_new_appbar)).waitTillViewIsDisplayed().perform(click())

        //Assertions
        //confirm toolbar title text
        onView(withId(R.id.topAppBar_normal)).check(
            matches(
                hasDescendant(
                    withText(
                        resources.getString(
                            R.string.add_income_category
                        )
                    )
                )
            )
        )
        //make sure editText is empty
        onView(withId(R.id.edt_categoryName)).check(matches(isDisplayed()))
            .check(matches(withText("")))
    }

    @Test
    fun shouldInsertNewExpensesCategory_whenNavigateToAddCategoryFragment(): Unit = runBlocking {
        val categoryName = "Test1Category!"
        //confirm there is not any category with 'categoryName' and expenses type
        assert(
            categoryByNameAndOrder(
                categoryName = categoryName,
                categoryType = Constants.EXPENSES_TYPE_MARKER
            ) == null
        )
        //navigate to category setting fragment
        //open up the drawer menu
        onView(withContentDescription(R.string.navigation_drawer_cd))
            .perform(click())
        //click on about us item in menu
        onView(withId(R.id.viewCategoriesFragment)).perform(click())

        //click on add new button to navigate to addCategoryFragment
        onView(withId(R.id.add_new_appbar)).perform(click())

        //Assertions

        //TODO check if keyboard is displayed
        //insert the name of category
        onView(withId(R.id.edt_categoryName)).perform(typeText(categoryName))
        //click on submit button
        onView(withId(R.id.add_category_fab)).perform(click())

        //check if category is actually inserted and showed in list of categories
        onView(
            withId(R.id.recycler_viewCategories),
        ).check(
            matches(
                atPositionOnView(
                    0,
                    withText(categoryName),//hardcoded name of category with order 0
                    R.id.nameOfCategory
                )
            )
        )
        //confirm category actually inserted into database
        assert(
            categoryByNameAndOrder(
                categoryName = categoryName,
                categoryType = Constants.EXPENSES_TYPE_MARKER
            ) != null
        )
    }

    @Test
    fun shouldInsertNewIncomeCategory_whenNavigateToAddCategoryFragment(): Unit = runBlocking {
        val categoryName = "Test2Category@"
        //confirm there is not any category with 'categoryName' and income type
        assert(
            categoryByNameAndOrder(
                categoryName = categoryName,
                categoryType = Constants.INCOME_TYPE_MARKER
            ) == null
        )
        //navigate to category setting fragment
        //open up the drawer menu
        onView(withContentDescription(R.string.navigation_drawer_cd))
            .perform(click())
        //click on about us item in menu
        onView(withId(R.id.viewCategoriesFragment)).perform(click())

        //swipe to income page of viewPager
        onView(withId(R.id.viewPager_viewCategories)).perform(customSwipeLeft())
        //click on add new button to navigate to addCategoryFragment
        onView(withId(R.id.add_new_appbar)).waitTillViewIsDisplayed().perform(click())

        //Assertions
        //TODO check if keyboard is displayed
        //insert the name of category
        onView(withId(R.id.edt_categoryName)).perform(typeText(categoryName))
        //click on submit button
        onView(withId(R.id.add_category_fab)).perform(click())

        //check if category is actually inserted and showed in list of categories
        onView(
            withId(R.id.recycler_viewCategories),
        ).check(
            matches(
                atPositionOnView(
                    0,
                    withText(categoryName),//hardcoded name of category with order 0
                    R.id.nameOfCategory
                )
            )
        )
        //confirm category actually inserted into database
        assert(
            categoryByNameAndOrder(
                categoryName = categoryName,
                categoryType = Constants.INCOME_TYPE_MARKER
            ) != null
        )
    }


    private suspend fun categoryByNameAndOrder(
        categoryName: String,
        categoryType: Int
    ): Category? {
        val allOfCategories = categoriesDao.getAllOfCategories()
        for (item in allOfCategories) {
            if (
                item.name == categoryName &&
                item.type == categoryType
            ) {
                return item
            }
        }
        return null
    }
}
