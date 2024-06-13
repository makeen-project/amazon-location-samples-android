package com.amazon.androidquickstartapp.ui.viewModel

import android.content.Context
import com.amazon.androidquickstartapp.utils.Constants.IDENTITY_POOL_ID
import com.amazon.androidquickstartapp.utils.Constants.INDEX_NAME
import com.amazon.androidquickstartapp.utils.Constants.MAP_NAME
import com.amazon.androidquickstartapp.utils.Constants.REGION
import com.amazon.androidquickstartapp.utils.Helper
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock


class MainViewModelValidationTest {

    private lateinit var viewModel: MainViewModel

    @Mock
    lateinit var context: Context

    @Mock
    private lateinit var mockHelper: Helper

    @Before
    fun setUp() {
        mockHelper = mockk()
        context = mockk(relaxed = true)
        viewModel = MainViewModel()
        viewModel.helper = mockHelper
    }

    @Test
    fun `test checkValidations when all fields are empty`() {
        every { mockHelper.showToast(any(), any()) } just runs
        viewModel.identityPoolId = ""
        viewModel.mapName  = ""
        viewModel.region = ""
        viewModel.indexName = ""

        assertEquals(true, viewModel.checkValidations(context))
    }

    @Test
    fun `test checkValidations when apiKey is empty`() {
        every { mockHelper.showToast(any(), any()) } just runs
        viewModel.identityPoolId = IDENTITY_POOL_ID
        viewModel.mapName = ""
        viewModel.region = REGION
        viewModel.indexName = INDEX_NAME

        assertEquals(true, viewModel.checkValidations(context))
    }

    @Test
    fun `test checkValidations when all fields are not empty`() {
        viewModel.identityPoolId = IDENTITY_POOL_ID
        viewModel.mapName = MAP_NAME
        viewModel.region = REGION
        viewModel.indexName = INDEX_NAME

        assertEquals(false, viewModel.checkValidations(context))
    }
}