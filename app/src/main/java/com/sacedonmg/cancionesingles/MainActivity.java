package com.sacedonmg.cancionesingles;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static com.sacedonmg.cancionesingles.UtilidadesCanciones.sincroListReproduccion;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private String LOG_TAG = "CI::MainActivity";

    public static int ACTIVIDAD_VISTA_CANCION_LOCAL = 4567;
    public static int ACTIVIDAD_VISTA_CANCION_REMOTA = 4568;
    public static int ACTIVIDAD_EDICION = 5678;

    public static final int SECCION_DESCARGADAS = 0;
    public static final int SECCION_REMOTAS = 1;

    private final int SECTIONS[] = {
            R.string.section_downloaded,
            R.string.section_availables,
    };

    private Context context;
    private FloatingActionButton floatingActionButton;
    private static TabLayout tabLayout;
    private Menu menu;
    private NavigationView navigationView;

    public static TabLayout getTabLayout() {
        return tabLayout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tabLayout = (TabLayout) findViewById(R.id.tabs);

        // Floating button
        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sincroListReproduccion();
                ListaCanciones.adaptador.notifyDataSetChanged();
            }
        });

        // Navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById( R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        context = this;

        setUserInfo();

        int screen = savedInstanceState != null ? savedInstanceState.getInt("SCREEN", -1) : R.id.nav_tabbed_activity;
        screen = screen == -1 ? R.id.nav_tabbed_activity : screen;
        if (screen ==  R.id.nav_signin) {
            Log.d(LOG_TAG, "R.id.nav_signin");
        } else {
            Log.d(LOG_TAG, "R.id.nav_tabbed_activity");
        }

        displaySelectedScreen(screen);
    }

    private void setUserInfo() {
        View headerLayout = navigationView.getHeaderView(0);
        TextView txtName = (TextView) headerLayout.findViewById(R.id.txtName);
        NetworkImageView fotoUsuario = (NetworkImageView) headerLayout.findViewById(R.id.imageView);

        FirebaseAuth auth = FirebaseSingleton.getInstance().getAuth();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            navigationView.getMenu().findItem(R.id.nav_signout).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_signin).setVisible(true);
            txtName.setText("");
            fotoUsuario.setImageUrl("", null);
            fotoUsuario.setDefaultImageResId(R.drawable.user);
            return;
        }

        navigationView.getMenu().findItem(R.id.nav_signout).setVisible(true);
        navigationView.getMenu().findItem(R.id.nav_signin).setVisible(false);
        // Nombre de usuario
        String name = currentUser.getDisplayName();
        txtName.setText(/*String.format(getString(R.string.welcome_message), name)*/name);

        // Foto de usuario
        Uri urlImagen = currentUser.getPhotoUrl();
        if (urlImagen != null) {
            fotoUsuario.setImageUrl(urlImagen.toString(), VolleySingleton.getInstance(this).getLectorImagenes());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        return true;
    }

    /***
     * Mostrar preferencias
     */
    public void lanzarPreferencias(){
        Intent i = new Intent(this, PreferenciasActivity.class);
        startActivity(i);
    }

    /**
     * Lanzar actividad Acerca De ...
     */
    public void lanzarAcercaDe(){
        Intent i = new Intent(this, AcercaDeActivity.class);
        startActivity(i);
    }

    /***
     * Lanza la actividad que permite insertar nuevas canciones
     */
    public void lanzarNuevo(){
        new AlertDialog.Builder(this)
                .setTitle(R.string.titulo_nuevo)
                .setMessage(R.string.mensaje_nuevo)
                .setPositiveButton(R.string.confirmar, new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int whichButton){
                        Intent i = new Intent (context, EdicionNuevaCancionActivity.class);
                        i.putExtra("editar",false);
                        startActivityForResult(i, ACTIVIDAD_EDICION);
                    }

                })
                .setNegativeButton(R.string.cancelar,null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
         if(id == R.id.nuevo){
            lanzarNuevo();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void logOut() {
        Log.d(LOG_TAG, "LOG-OUT");
        AuthUI.getInstance().signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        SharedPreferences pref = getSharedPreferences("com.sacedonmg.cancionesingles_internal", MODE_PRIVATE);
                        pref.edit().remove("provider").commit();
                        pref.edit().remove("email").commit();
                        pref.edit().remove("name").commit();
                        setUserInfo();
                        displaySelectedScreen(R.id.nav_tabbed_activity);
                    }
                });
    }

    public void displaySelectedScreen(int itemId) {
        Fragment fragment = null;
        switch (itemId) {
            case R.id.nav_signin:
                Log.d(LOG_TAG, "R.id.nav_signin");
                floatingActionButton.setVisibility(View.GONE);
                // menu.findItem(R.id.nuevo).setVisible(false); TODO
                fragment = new LoginActivity();
                break;
            case R.id.nav_tabbed_activity:
                Log.d(LOG_TAG, "nav_tabbed_activity");
                floatingActionButton.setVisibility(View.VISIBLE);
                // menu.findItem(R.id.nuevo).setVisible(true); TODO
                fragment = new TabbedActivity();
                break;
        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Log.d(LOG_TAG, "onNavigationItemSelected");

        int id = item.getItemId();
        if (id == R.id.nav_signin) {
            displaySelectedScreen(R.id.nav_signin);
        } else if (id == R.id.nav_signout) {
            logOut();
        } else if (id == R.id.action_settings) {
            lanzarPreferencias();
            return true;
        } else if (id == R.id.acercaDe){
            lanzarAcercaDe();
            return true;
        } else {
            displaySelectedScreen(id);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById( R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
