package it.ap.licensing;
import android.content.Context;
public class Licensing {
public boolean isDummy() { return true; }
public boolean isRegistered(Context ctx) { return true; }
public boolean doCheck(final Context ctx, final String token, final Runnable failer, final Runnable grace, final Runnable ok) { return true; }
}

