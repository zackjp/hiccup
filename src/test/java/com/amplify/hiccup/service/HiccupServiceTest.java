package com.amplify.hiccup.service;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.SparseArray;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(RobolectricTestRunner.class)
public class HiccupServiceTest {

    private static final String ROUTE_ONE = "/path/to/collection";
    private static final String ROUTE_TWO = "/path/to/collection/resource";
    private static final String AUTHORITY = "com.authority.name";
    private static final String METHOD = "method";

    private HiccupService hiccupService;

    @Mock
    private UriMatcher uriMatcher;
    @Mock
    private SparseArray<Controller> controllerMap;
    @Mock
    private Controller controller;
    @Mock
    private HttpCursorFactory httpCursorFactory;
    @Mock
    private Uri uri;

    @Before
    public void setUp() {
        initMocks(this);
        hiccupService = new HiccupService(AUTHORITY, uriMatcher, controllerMap, httpCursorFactory);
    }

    @Test
    public void shouldAddMultipleRoutes() {
        hiccupService.newRoute(ROUTE_ONE, controller);
        verify(uriMatcher).addURI(AUTHORITY, ROUTE_ONE, 1);
        verify(controllerMap).put(1, controller);

        hiccupService.newRoute(ROUTE_TWO, controller);
        verify(uriMatcher).addURI(AUTHORITY, ROUTE_TWO, 2);
        verify(controllerMap).put(2, controller);
    }

    @Test
    public void shouldDelegateGetRequestToMatchingControllerForRoute() {
        Object model = new Object();
        Cursor expectedCursor = mock(Cursor.class);
        when(controllerMap.get(anyInt())).thenReturn(controller);
        when(controller.get(uri)).thenReturn(model);
        when(httpCursorFactory.createCursor(model)).thenReturn(expectedCursor);

        Cursor actualCursor = hiccupService.delegateQuery(uri);

        assertThat(actualCursor, is(expectedCursor));
    }

    @Test
    public void shouldDelegatePostRequestToMatchingControllerForRoute() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("method", "POST");
        Uri expectedUri = mock(Uri.class);
        when(controllerMap.get(anyInt())).thenReturn(controller);
        when(controller.post(uri, contentValues)).thenReturn(expectedUri);

        Uri actualUri = hiccupService.delegateInsert(uri, contentValues);

        assertThat(actualUri, is(expectedUri));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowUnsupportedOperationExceptionForUnknownMethod() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(METHOD, "ASDF");

        hiccupService.delegateInsert(uri, contentValues);
    }
}
