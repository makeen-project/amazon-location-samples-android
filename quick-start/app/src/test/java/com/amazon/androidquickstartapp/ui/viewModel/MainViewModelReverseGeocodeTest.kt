package com.amazon.androidquickstartapp.ui.viewModel

import android.content.Context
import aws.sdk.kotlin.services.location.LocationClient
import aws.sdk.kotlin.services.location.model.Place
import aws.sdk.kotlin.services.location.model.SearchForPositionResult
import aws.sdk.kotlin.services.location.model.SearchPlaceIndexForPositionResponse
import com.amazon.androidquickstartapp.utils.Constants.EXPECTED_LABEL
import io.mockk.coEvery

import io.mockk.mockk
import io.mockk.mockkConstructor
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.maplibre.android.geometry.LatLng
import org.mockito.Mock
import com.amazon.androidquickstartapp.utils.AmazonLocationClient
import software.amazon.location.auth.LocationCredentialsProvider


class MainViewModelReverseGeocodeTest {

    private lateinit var viewModel: MainViewModel

    @Mock
    private lateinit var locationCredentialsProvider: LocationCredentialsProvider

    @Mock
    lateinit var context: Context

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        locationCredentialsProvider = mockk()
        viewModel = MainViewModel()
        mockkConstructor(LocationCredentialsProvider::class)
        mockkConstructor(AmazonLocationClient::class)
    }

    @Test
    fun `test reverseGeocode`() {
        val mockAmazonLocationClient = mockk<LocationClient>()
        coEvery {
            locationCredentialsProvider.getLocationClient()
        } returns mockAmazonLocationClient
        val searchPlaceIndexForPositionResponse = SearchPlaceIndexForPositionResponse.invoke {
            results = listOf(SearchForPositionResult.invoke {
                distance = 20.0
                placeId = "11"
                place = Place.invoke {
                    label = EXPECTED_LABEL
                }
            })
            summary = null
        }
        viewModel.locationCredentialsProvider = locationCredentialsProvider
        coEvery { mockAmazonLocationClient.searchPlaceIndexForPosition(any()) } returns searchPlaceIndexForPositionResponse
        val latLng = LatLng(37.7749, -122.4194)

        runBlocking {
            val label = viewModel.reverseGeocode(latLng)

            assertEquals(EXPECTED_LABEL, label)
        }
    }
}