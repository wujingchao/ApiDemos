/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.apis.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SimpleCursorAdapter;
import android.widget.SearchView.OnQueryTextListener;

/**
 * Demonstration of the use of a CursorLoader to load and display contacts data in a fragment.
 * Creates a custom class CursorLoaderListFragment which extends ListFragment, with the necessary
 * callbacks to serve as a CursorLoader to load and display contacts data in the ListFragment.
 * Includes the use of a SearchView which might come in handy for MarkovChain
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class LoaderCursor extends Activity {
    final static String TAG = "LoaderCursor";

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * onCreate, then we initialize {@code FragmentManager fm} with a reference to the FragmentManager
     * for interacting with fragments associated with this activity. We check whether this is the
     * first time we are being created by using {@code fm} to search for a Fragment with the container
     * view ID android.R.id.content (our root view) and if it does not find one we create an instance
     * of {@code CursorLoaderListFragment list} and use {@code fm} begin a {@code FragmentTransaction}
     * to add the fragment {@code list} to the Activity's state using the container view
     * android.R.id.content, and commit the {@code FragmentTransaction}. If there was already a
     * Fragment occupying android.R.id.content we are being recreated after an orientation change and
     * do not need to do anything.
     *
     * @param savedInstanceState we do not override onSaveInstanceState so do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager fm = getFragmentManager();

        // Create the list fragment and add it as our sole content.
        if (fm.findFragmentById(android.R.id.content) == null) {
            CursorLoaderListFragment list = new CursorLoaderListFragment();
            fm.beginTransaction().add(android.R.id.content, list).commit();
        } else {
            Log.i(TAG, "There is already a Fragment occupying our root view");
        }
    }

    /**
     * This {@code ListFragment} fills its {@code List} with data returned from a {@code SimpleCursorAdapter}
     */
    public static class CursorLoaderListFragment extends ListFragment
            implements OnQueryTextListener, OnCloseListener,
            LoaderManager.LoaderCallbacks<Cursor> {

        // This is the Adapter being used to display the list's data.
        SimpleCursorAdapter mAdapter;

        // The SearchView for doing filtering.
        SearchView mSearchView;

        // If non-null, this is the current filter the user has provided.
        String mCurFilter;

        /**
         * Called when the fragment's activity has been created and this fragment's view hierarchy
         * instantiated. First we call through to our super's implementaton of {@code onActivityCreated},
         * Then we set the text for the {@code TextView} of the default content for our {@code ListFragment}
         * to the String "No phone numbers". Then we report that this fragment would like to participate
         * in populating the options menu by receiving a call to onCreateOptionsMenu(Menu, MenuInflater)
         * and related methods. Next we create an instance of {@code SimpleCursorAdapter} with a null
         * cursor (we will set the cursor for the adapter later) and save a reference to it in our
         * field {@code SimpleCursorAdapter mAdapter}, which we then use to set our ListView's adapter.
         * We call {@code setListShown(false)} so that a indeterminate progress bar will be displayed
         * while we wait fot our Adapter to have data available for the List. Finally we start a
         * loader (or reconnect) specifying {@code this} as the {@code LoaderManager.LoaderCallbacks<Cursor>}
         * interface provider.
         *
         * @param savedInstanceState we do not override onSaveInstanceState so do not use
         */
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            // Give some text to display if there is no data.  In a real
            // application this would come from a resource.
            setEmptyText("No phone numbers");

            // We have a menu item to show in action bar.
            setHasOptionsMenu(true);

            // Create an empty adapter we will use to display the loaded data.
            mAdapter = new SimpleCursorAdapter(getActivity(),
                    android.R.layout.simple_list_item_2, null,
                    new String[]{Contacts.DISPLAY_NAME, Contacts.CONTACT_STATUS},
                    new int[]{android.R.id.text1, android.R.id.text2}, 0);
            setListAdapter(mAdapter);

            // Start out with a progress indicator.
            setListShown(false);

            // Prepare the loader.  Either re-connect with an existing one,
            // or start a new one.
            getLoaderManager().initLoader(0, null, this);
        }

        /**
         * Custom {@code SearchView} which clears the search text when the {@code SearchView} is
         * collapsed by the user.
         */
        public static class MySearchView extends SearchView {
            /**
             * Constructor which simply calls our super's constructor.
             *
             * @param context Context used by super ({@code getActivity()} called from menu of our
             *                {@code CursorLoaderListFragment} would return {@code LoaderCurstor}
             *                as the Activity Context in our case)
             */
            public MySearchView(Context context) {
                super(context);
            }

            /**
             * Called when this view is collapsed as an action view.
             * See {@link MenuItem#collapseActionView()}.
             * <p>
             * The normal SearchView doesn't clear its search text when collapsed, so we will do
             * this for it. We simply call {@code setQuery} with an empty String, and false to
             * prevent it being looked up. Finally we call through to our super's implementation
             * of {@code onActionViewCollapsed}.
             */
            @Override
            public void onActionViewCollapsed() {
                setQuery("", false);
                super.onActionViewCollapsed();
            }
        }

        /**
         * Initialize the contents of the Activity's standard options menu.  You
         * should place your menu items in to <var>menu</var>.  For this method
         * to be called, you must have first called {@link #setHasOptionsMenu}.  See
         * {@link Activity#onCreateOptionsMenu(Menu) Activity.onCreateOptionsMenu}
         * for more information.
         * <p>
         * First we add a new {@code MenuItem item} to {@code Menu menu} with the title "Search", we
         * set the icon for {@code item} to the android system drawable ic_menu_search, set the options
         * for {@code item} to SHOW_AS_ACTION_IF_ROOM and SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW (the
         * items action view will collapse to a normal menu item).
         * <p>
         * Next we initialize our field {@code SearchView mSearchView} with a new instance of
         * {@code MySearchView}, set its {@code SearchView.OnQueryTextListener} to "this", set its
         * {@code SearchView.OnCloseListener} to this, and set the default or resting state of the
         * search field to iconified (a single search icon is shown by default and expands to show
         * the text field and other buttons when pressed. Also, if the default state is iconified,
         * then it collapses to that state when the close button is pressed.
         * <p>
         * Finally we set the action view of {@code MenuItem item} to {@code mSearchView}.
         *
         * @param menu     The options menu in which you place your items.
         * @param inflater could be used to instantiate menu XML files into Menu objects, but we do
         *                 not use
         * @see #setHasOptionsMenu
         * @see #onPrepareOptionsMenu
         * @see #onOptionsItemSelected
         */
        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // Place an action bar item for searching.
            MenuItem item = menu.add("Search");
            item.setIcon(android.R.drawable.ic_menu_search);
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
                    | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
            mSearchView = new MySearchView(getActivity());
            mSearchView.setOnQueryTextListener(this);
            mSearchView.setOnCloseListener(this);
            mSearchView.setIconifiedByDefault(true);
            item.setActionView(mSearchView);
        }

        /**
         * Called when the query text is changed by the user. If the new text is not empty we set
         * {@code String newFilter} to it otherwise we set it to null. If both {@code newFilter}
         * and our current filter {@code String mCurFilter} are null we do nothing and return true
         * to the caller. If {@code mCurFilter} is not null and {@code newFilter} is equal to it
         * we also do nothing and return true to the caller.
         * <p>
         * If we get this far, the user has changed the filter that we should be using, so we set
         * {@code mCurFilter} to {@code newFilter}, and use the {@code LoaderManager} for this
         * Fragment to restart our loader (which eventually will result in our override of
         * {@code onCreateLoader} being called). Finally we return true to the caller.
         *
         * @param newText the new content of the query text field.
         * @return false if the SearchView should perform the default action of showing any
         * suggestions if available, true if the action was handled by the listener. We always
         * return true.
         */
        @Override
        public boolean onQueryTextChange(String newText) {
            // Called when the action bar search text has changed.  Update
            // the search filter, and restart the loader to do a new query
            // with this filter.
            String newFilter = !TextUtils.isEmpty(newText) ? newText : null;
            // Don't do anything if the filter hasn't actually changed.
            // Prevents restarting the loader when restoring state.
            if (mCurFilter == null && newFilter == null) {
                return true;
            }
            if (mCurFilter != null && mCurFilter.equals(newFilter)) {
                return true;
            }
            mCurFilter = newFilter;
            getLoaderManager().restartLoader(0, null, this);
            return true;
        }

        /**
         * Called when the user submits the query. We do not use this feature so we just return true
         * to the caller to signify that we have "handled" the query.
         *
         * @param query the query text that is to be submitted
         * @return true if the query has been handled by the listener, false to let the
         * SearchView perform the default action.
         */
        @Override
        public boolean onQueryTextSubmit(String query) {
            // Don't care about this.
            return true;
        }

        /**
         * The user is attempting to close the SearchView. We check to see if there are currently any
         * characters in the text field of our {@code SearchView mSearchView} and if there are we
         * set the query of {@code SearchView mSearchView} to null and submit the query. Finally we
         * return true to the caller (since we wanted to override the default behavior)
         *
         * @return true if the listener wants to override the default behavior of clearing the
         * text field and dismissing it, false otherwise.
         */
        @Override
        public boolean onClose() {
            if (!TextUtils.isEmpty(mSearchView.getQuery())) {
                mSearchView.setQuery(null, true);
            }
            return true;
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            // Insert desired behavior here.
            Log.i("FragmentComplexList", "Item clicked: " + id);
        }

        // These are the Contacts rows that we will retrieve.
        static final String[] CONTACTS_SUMMARY_PROJECTION = new String[]{
                Contacts._ID,
                Contacts.DISPLAY_NAME,
                Contacts.CONTACT_STATUS,
                Contacts.CONTACT_PRESENCE,
                Contacts.PHOTO_ID,
                Contacts.LOOKUP_KEY,
        };

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            // This is called when a new Loader needs to be created.  This
            // sample only has one Loader, so we don't care about the ID.
            // First, pick the base URI to use depending on whether we are
            // currently filtering.
            Uri baseUri;
            if (mCurFilter != null) {
                baseUri = Uri.withAppendedPath(Contacts.CONTENT_FILTER_URI,
                        Uri.encode(mCurFilter));
            } else {
                baseUri = Contacts.CONTENT_URI;
            }

            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            String select = "((" + Contacts.DISPLAY_NAME + " NOTNULL) AND ("
                    + Contacts.HAS_PHONE_NUMBER + "=1) AND ("
                    + Contacts.DISPLAY_NAME + " != '' ))";
            return new CursorLoader(getActivity(), baseUri,
                    CONTACTS_SUMMARY_PROJECTION, select, null,
                    Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            // Swap the new cursor in.  (The framework will take care of closing the
            // old cursor once we return.)
            mAdapter.swapCursor(data);

            // The list should now be shown.
            if (isResumed()) {
                setListShown(true);
            } else {
                setListShownNoAnimation(true);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            // This is called when the last Cursor provided to onLoadFinished()
            // above is about to be closed.  We need to make sure we are no
            // longer using it.
            mAdapter.swapCursor(null);
        }
    }

}
