package com.amazon.androidquickstartapp.ui.viewModel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import com.amazon.androidquickstartapp.utils.Helper
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock


class MainViewModelPermissionTest {

    private lateinit var viewModel: MainViewModel

    @Mock
    lateinit var mockContext: Context

    @Mock
    private lateinit var mockHelper: Helper

    @Before
    fun setUp() {
        mockHelper = mockk()
        mockContext = mockk(relaxed = true)
        viewModel = MainViewModel()
        viewModel.helper = mockHelper
    }

    @Test
    fun `test checkLocationPermission when permissions are granted`() {
        every { mockContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) } returns PackageManager.PERMISSION_GRANTED
        every { mockContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) } returns PackageManager.PERMISSION_GRANTED

        assertEquals(false, viewModel.checkLocationPermission(mockContext))
    }
}