
## Overview

Hiccup is an Android library that layers HTTP semantics in ContentProviders to
simplify data access and improve performance.

It lets UIs communicate with ContentProviders using GET/POST/etc semantics. It
*does not* make actual HTTP requests to the internet. One benefit of this is that
the UI becomes decoupled from the persistence implementation (eg, sqlite) and does
not need to know about WHERE clauses, etc.

The idea is to approach ContentProviders more like RESTful web service apis;
they are so similar that we can borrow some established best practices,
including RESTful interfaces and MVC-like concepts.

#### Goals

1. improve code maintainability of ContentProviders
1. separate client/provider concerns
1. allow UI/data layers to evolve independently
1. expose a clean & simple api for developers
1. remain compatible within normal ContentProvider behavior

#### Status
Alpha, work in progress. Use at your own discretion. Currently supports:

* GET
* POST
* PUT
* DELETE

#### Motivation

Android's ContentProvider is a great tool for data access and cross-app sharing,
but its interface is confusing and has several limitations. Mainly, its
interface is half REST and half SQL, which result in the following:

1. clients (eg, Activities) are forced to know underlying db schema via sql projections, where clauses, etc.
1. restructuring tables (data normalization) breaks client code
1. db transactions, joins, cascades, etc. are difficult to support when using uri's + sql
1. data integrity is difficult to maintain when it's left up to clients
1. assumes sqlite backend (ie, does not easily support in-memory, file, shared pref, NoSQL, etc.)
1. data/resources cannot be versioned

## Usage

#### Client (eg, Activity) makes GET/POST/PUT/DELETE requests

```Java
@Override
public void onCreate() {
    super.onCreate();
    setContentView(R.layout.whatever);

    findViewById(R.id.delete_button).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Uri uri = Uri.parse("content://com.your.authority/posts/" + postId);
                hiccupClient.delete(uri); // on UI thread just for demo purposes
            }
    });
}
```

#### ContentProvider dispatches requests to controllers

```Java
private HiccupService hiccupService;

@Override
public boolean onCreate() {
    super.onCreate();
    SQLiteOpenHelper dbHelper = createDbHelper();
    hiccupService = new HiccupService("com.your.authority")
            .newRoute("posts/#", new PostsController(dbHelper))
            .newRoute("posts/#/comments", new CommentsController(dbHelper))
            // etc
            ;
}

@Override
public int delete(Uri uri, String selection, String[] selectionArgs) {
    // Delegate to Controller based on incoming uri and registered #newRoute()'s
    // (intentionally ignores the selection+args)
    return hiccupService.delegateDelete(uri);
}
```

#### Controller handles transactions, data integrity, etc.

```Java
public class PostsController implements Controller {
    // constructor, etc...

    @Override
    public int delete(Uri uri) {
        String postId = uri.getPathSegments().get(1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete("comments", "post_id = ?", new String[]{postId});
            db.delete("posts", "_id = ?", new String[]{postId});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        Uri changedUri = Uri.parse("content://com.your.authority/posts");
        contentResolver.notifyChange(changedUri, null);
    }
}
```
