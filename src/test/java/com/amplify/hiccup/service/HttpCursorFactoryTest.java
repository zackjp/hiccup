package com.amplify.hiccup.service;

import android.database.Cursor;
import android.provider.BaseColumns;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(RobolectricTestRunner.class)
public class HttpCursorFactoryTest {

    private HttpCursorFactory factory;

    @Mock
    private JsonConverter jsonConverter;

    @Before
    public void setUp() {
        initMocks(this);

        factory = new HttpCursorFactory(jsonConverter);
    }

    @Test
    public void shouldConvertSingleModelIntoCursorWithSingleRow() {
        Cursor cursor = factory.createCursor(new Object());

        assertThat(cursor.getCount(), is(1));
    }

    @Test
    public void shouldConvertMultipleModelsIntoCursorWithMultipleRows() {
        Iterable<?> someList = Arrays.asList(new Object(), new Object(), new Object());

        Cursor cursor = factory.createCursor(someList);

        assertThat(cursor.getCount(), is(3));
    }

    @Test
    public void shouldConvertSingleModelIntoCursorWithBaseColumnId() {
        Cursor cursor = factory.createCursor(new Object());

        int idColumn = cursor.getColumnIndex(BaseColumns._ID);
        cursor.moveToFirst();
        assertThat(cursor.getLong(idColumn), is(1L));
    }

    @Test
    public void shouldConvertSingleModelIntoCursorWithJsonBody() {
        String expectedJsonBody = "{\"aKey\" : \"some value\"}";
        Object dbModel = new Object();
        when(jsonConverter.toJson(dbModel)).thenReturn(expectedJsonBody);

        Cursor cursor = factory.createCursor(dbModel);

        int bodyColumn = cursor.getColumnIndex("body");
        cursor.moveToFirst();
        String actualBody = cursor.getString(bodyColumn);
        assertThat(actualBody, is(expectedJsonBody));
    }

    @Test
    public void shouldConvertMultipleModelsIntoCursorWithJsonBody() {
        String expectedJsonBody = "{\"someKey\" : \"a value\"}";
        Object dbModel = new Object();
        Iterable<?> modelItems = Arrays.asList(dbModel);
        when(jsonConverter.toJson(dbModel)).thenReturn(expectedJsonBody);

        Cursor cursor = factory.createCursor(modelItems);

        int bodyColumn = cursor.getColumnIndex("body");
        cursor.moveToFirst();
        String actualBody = cursor.getString(bodyColumn);
        assertThat(actualBody, is(expectedJsonBody));
    }

}