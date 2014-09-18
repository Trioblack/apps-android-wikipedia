package org.wikipedia.beta.nearby;

import android.content.Context;
import android.location.Location;
import org.json.JSONObject;
import org.mediawiki.api.json.Api;
import org.mediawiki.api.json.ApiException;
import org.mediawiki.api.json.ApiResult;
import org.mediawiki.api.json.RequestBuilder;
import org.wikipedia.beta.ApiTask;
import org.wikipedia.beta.Site;
import org.wikipedia.beta.Utils;
import org.wikipedia.beta.WikipediaApp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Actual work to search for nearby pages.
 */
public class NearbyFetchTask extends ApiTask<List<NearbyPage>> {
    /** search radius in meters. 10 km is the maximum the API allows. */
    private static final String RADIUS = "10000";
    /** max number of results */
    private static final String LIMIT = "50";
    /** requested thumbnail size in pixel */
    private static final String THUMBNAIL_WIDTH = "144";
    private final WikipediaApp app;
    private final Location location;

    public NearbyFetchTask(Context context, Site site, Location location) {
        super(
                SINGLE_THREAD,
                ((WikipediaApp) context.getApplicationContext()).getAPIForSite(site)
        );
        this.app = (WikipediaApp) context.getApplicationContext();
        this.location = location;
    }

    @Override
    public RequestBuilder buildRequest(Api api) {
        return api.action("query")
                .param("prop", "coordinates|pageimages")
                .param("colimit", LIMIT)
                .param("pithumbsize", THUMBNAIL_WIDTH)
                .param("pilimit", LIMIT)
                .param("generator", "geosearch")
                .param("ggscoord", locationParam(location))
                .param("ggsradius", RADIUS)
                .param("ggslimit", LIMIT)
                .param("format", "json");
    }

    private String locationParam(Location location) {
        return String.format(Locale.ROOT, "%f|%f", location.getLatitude(), location.getLongitude());
    }

    /*
    // returns data formatted as follows:
        {
            coordinates = {
                globe = earth;
                lat = "51.5202";
                lon = "-0.095";
                primary = "";
            };
            distance = "207.4141690965082";
            pageid = 26536263;
            pageimage = "Barbican_Centre_logo.svg";
            thumbnail =             {
                height = 38;
                source = "https://upload.wikimedia.org/wikipedia/en/thumb/e/e3/Barbican_Centre_logo.svg/50px-Barbican_Centre_logo.svg.png";
                width = 50;
            };
            title = "Barbican Centre";
        },
        {
            coordinates = {
                globe = earth;
                lat = "51.5191";
                lon = "-0.096946";
                primary = "";
            };
            distance = "324.2053114467474";
            pageid = 2303936;
            pageimage = "William_Davenant.jpg";
            thumbnail =             {
                height = 50;
                source = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/ae/William_Davenant.jpg/33px-William_Davenant.jpg";
                width = 33;
            };
            title = "Rutland House";
        },
        ...
*/


    @Override
    public List<NearbyPage> processResult(ApiResult result) throws Throwable {
        ArrayList<NearbyPage> list = new ArrayList<NearbyPage>();

        try {
            if (result.asObject().has("error")) {
                JSONObject errorJSON = result.asObject().optJSONObject("error");
                throw new NearbyFetchException(errorJSON.optString("code"), errorJSON.optString("info"));
            }

            final JSONObject pagesMap = result.asObject().optJSONObject("query").optJSONObject("pages");
            Iterator iterator = pagesMap.keys();

            while (iterator.hasNext()) {
                NearbyPage newPage = new NearbyPage(pagesMap.getJSONObject((String) iterator.next()));
                list.add(newPage);
            }
        } catch (ApiException e) {
            // TODO: find a better way to deal with empty results
            if (!e.getCause().getMessage().startsWith("Value []")) {
                throw e;
            }
        }

        if (WikipediaApp.isWikipediaZeroDevmodeOn()) {
            Utils.processHeadersForZero(app, result);
        }

        return list;
    }
}