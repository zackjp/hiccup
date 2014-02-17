package com.amplify.hiccup.client;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import com.amplify.hiccup.service.JsonConverter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(RobolectricTestRunner.class)
public class HiccupClientTest {

    private static final String METHOD = "method";
    private static final String BODY = "body";

    private HiccupClient hiccupClient;

    @Mock
    private Context context;
    @Mock
    private ContentResolver contentResolver;
    @Mock
    private Uri uri;
    @Mock
    private JsonConverter jsonConverter;

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        given(context.getContentResolver()).willReturn(contentResolver);

        hiccupClient = new HiccupClient(context, jsonConverter);
    }

    @Test
    public void shouldSendUriRequestViaContentResolver() {
        Cursor expectedCursor = mock(Cursor.class);
        when(contentResolver.query(uri, null, null, null, null)).thenReturn(expectedCursor);

        Cursor actualCursor = hiccupClient.get(uri);

        assertThat(actualCursor, is(expectedCursor));
    }

    @Test
    public void shouldInsertAsPostToContentProviderWithPostMethod() {
        hiccupClient.post(uri, new Object());

        ArgumentCaptor<ContentValues> captor = ArgumentCaptor.forClass(ContentValues.class);
        verify(contentResolver).insert(eq(uri), captor.capture());
        ContentValues contentValues = captor.getValue();
        assertThat(contentValues.getAsString(METHOD), is("POST"));
    }

    @Test
    public void shouldInsertAsPostToContentProviderWithJsonModel() {
        Object model = new Object();
        String expectedJson = "Lois, this is not my Batman glass.";
        when(jsonConverter.toJson(model)).thenReturn(expectedJson);

        hiccupClient.post(uri, model);

        ArgumentCaptor<ContentValues> captor = ArgumentCaptor.forClass(ContentValues.class);
        verify(contentResolver).insert(eq(uri), captor.capture());
        ContentValues contentValues = captor.getValue();
        assertThat(contentValues.getAsString(BODY), is(expectedJson));
    }

    @Test
    public void shouldReturnUriFromInsertAsPostToContentProvider() {
        Uri expectedUri = mock(Uri.class);
        when(contentResolver.insert(eq(uri), any(ContentValues.class))).thenReturn(expectedUri);

        Uri actualUri = hiccupClient.post(uri, new Object());

        assertThat(actualUri, is(expectedUri));
    }
}
