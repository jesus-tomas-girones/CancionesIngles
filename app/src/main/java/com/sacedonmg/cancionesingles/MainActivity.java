package com.sacedonmg.cancionesingles;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
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
import com.google.firebase.auth.FirebaseUser;

import java.io.File;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static com.sacedonmg.cancionesingles.Utilidades.isPermissionGranted;
import static com.sacedonmg.cancionesingles.Utilidades.requestPermission;
import static com.sacedonmg.cancionesingles.UtilidadesCanciones.crearArchivosEjemplo;
import static com.sacedonmg.cancionesingles.UtilidadesCanciones.mostrarMensaje;
import static com.sacedonmg.cancionesingles.UtilidadesCanciones.rutaCarpeta;
import static com.sacedonmg.cancionesingles.UtilidadesCanciones.sincroListReproduccion;
import static com.sacedonmg.cancionesingles.UtilidadesCanciones.validarLeerSD;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private String LOG_TAG = "CI::MainActivity";

    // REQUEST CODES
    public static int ACTIVIDAD_VISTA_CANCION_LOCAL = 4567;
    public static int ACTIVIDAD_VISTA_CANCION_REMOTA = 4568;
    public static int ACTIVIDAD_CREAR = 5678;
    public static int ACTIVIDAD_EDICION = 1234;
    public static int ACTIVIDAD_ETIQUETAR = 2345;
    public static final int ACTIVIDAD_LOGIN = 2346;
    public static final int ACTIVIDAD_TABBED = 2347;
    public static final int WRITE_EXTERNAL_STORAGE_PERMISSION = 3000;
    public static final int READ_EXTERNAL_STORAGE_PERMISSION = 3001;

    // RESULT CODES
    public static int CANCION_DESCARGADA = 1001;
    public static int EDITAR_OK = 1002;
    public static int BORRAR_OK = 1002;
    public static int CREAR_OK = 1002;
    public static int LOGIN_SUCCESS = 1003;

    public static final int SECCION_DESCARGADAS = 0;
    public static final int SECCION_REMOTAS = 1;

    private final int SECTIONS[] = {
            R.string.section_downloaded,
            R.string.section_availables,
    };

    private Context context;
    private FloatingActionButton floatingActionButton;
    private static TabLayout tabLayout;
    private NavigationView navigationView;

    public static TabLayout getTabLayout() {
        return tabLayout;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == READ_EXTERNAL_STORAGE_PERMISSION ||requestCode == WRITE_EXTERNAL_STORAGE_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && validarLeerSD()) {
                File carpeta = new File(rutaCarpeta);
                if (!carpeta.exists()) {  //Es la primera vez que se instala la aplicación.
                    carpeta.mkdirs();
                    crearArchivosEjemplo(this);
                }
            }
        }

        sincroListReproduccion();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void sincronizarContenidoSD() {
        if (isPermissionGranted(READ_EXTERNAL_STORAGE, this)) {
            sincroListReproduccion();
            ListaCanciones.adaptador.notifyDataSetChanged();
        } else  {
          requestPermission(READ_EXTERNAL_STORAGE, this);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_navigation_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tabLayout = (TabLayout) findViewById(R.id.tabs);

        // Floating button
        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewPager viewPager = TabbedActivity.getViewPager();
                if (viewPager == null) {
                    return;
                }

                if (viewPager.getCurrentItem() == SECCION_DESCARGADAS) {
                    sincronizarContenidoSD();
                }

                if (viewPager.getCurrentItem() == SECCION_REMOTAS) {
                    ListaCancionesRemoto.adaptador.notifyDataSetChanged();
                }
            }
        });

        // Navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById( R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        Bundle bundle = getIntent().getExtras();
        int result = bundle != null ? bundle.getInt("result", -1) : -1;
        setUserInfo(result);

        int screen = bundle != null ? bundle.getInt("SCREEN", -1) : -1;
        screen = screen != -1 ? screen : ACTIVIDAD_TABBED;
        displaySelectedScreen(screen);
    }

    private void setUserInfo(int result) {
        View headerLayout = navigationView.getHeaderView(0);
        TextView txtName = (TextView) headerLayout.findViewById(R.id.txtName);
        TextView txtEmail = (TextView) headerLayout.findViewById(R.id.txtEmail);
        NetworkImageView fotoUsuario = (NetworkImageView) headerLayout.findViewById(R.id.imageView);

        FirebaseUser currentUser = FirebaseSingleton.getInstance().getCurrentUser();
        if (currentUser == null) {
            navigationView.getMenu().findItem(R.id.nav_signout).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_signin).setVisible(true);
            txtName.setText("");
            fotoUsuario.setImageUrl(null, null);
            fotoUsuario.setDefaultImageResId(R.drawable.user);
            return;
        }

        // Nombre de usuario
        String name = currentUser.getDisplayName();
        String email = currentUser.getEmail();
        txtName.setText(name);
        txtEmail.setText(email);

        // Foto de usuario
        Uri urlImagen = currentUser.getPhotoUrl();
        if (urlImagen != null) {
            fotoUsuario.setImageUrl(urlImagen.toString(), VolleySingleton.getInstance(this).getLectorImagenes());
        } else {
            fotoUsuario.setDefaultImageResId(R.drawable.user);
        }

        if (result == LOGIN_SUCCESS) {
            name = name != null ? name : email;
            name = name.split("@")[0];
            mostrarMensaje(this, "Bienvenido " + name);
        }

        navigationView.getMenu().findItem(R.id.nav_signout).setVisible(true);
        navigationView.getMenu().findItem(R.id.nav_signin).setVisible(false);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
                        startActivityForResult(i, ACTIVIDAD_CREAR);
                    }

                })
                .setNegativeButton(R.string.cancelar,null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        ViewPager viewPager = TabbedActivity.getViewPager();
        if (viewPager == null) {
            return false;
        }

         if(id == R.id.nuevo){
            lanzarNuevo();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void logOut() {
        Log.d(LOG_TAG, "LOG OUT");
        AuthUI.getInstance().signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        setUserInfo(-1);
                        mostrarMensaje(MainActivity.this, "¡Hasta pronto!");
                        displaySelectedScreen(ACTIVIDAD_TABBED);
                    }
                });
    }

    public void displaySelectedScreen(int itemId) {
        Fragment fragment = null;
        switch (itemId) {
            case ACTIVIDAD_LOGIN:
                Log.d(LOG_TAG, "R.id.nav_signin");
                floatingActionButton.setVisibility(View.GONE);
                fragment = new LoginActivity();
                break;
            case ACTIVIDAD_TABBED:
                Log.d(LOG_TAG, "nav_tabbed_activity");
                floatingActionButton.setVisibility(View.VISIBLE);
                fragment = new TabbedActivity();
                break;
        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment).commit();
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
            displaySelectedScreen(ACTIVIDAD_LOGIN);
        } else if (id == R.id.nav_tabbed_activity) {
            displaySelectedScreen(ACTIVIDAD_TABBED);
        } else if (id == R.id.nav_signout) {
            logOut();
        } else if (id == R.id.action_settings) {
            lanzarPreferencias();
            return true;
        } else if (id == R.id.acercaDe) {
            lanzarAcercaDe();
            return true;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById( R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "onActivityResult");
        int posicion = data != null && data.hasExtra("posicion") ? data.getIntExtra("posicion", -1) : -1;
        if (resultCode == CREAR_OK || resultCode == CANCION_DESCARGADA) ListaCanciones.adaptador.notifyItemInserted(CancionesVector.getInstance().tamanyo() - 1);
        else if (resultCode == EDITAR_OK && posicion >= 0) ListaCanciones.adaptador.notifyItemChanged(posicion);
        else if (resultCode == BORRAR_OK && posicion >= 0) ListaCanciones.adaptador.notifyItemRemoved(posicion);
        if (resultCode == CANCION_DESCARGADA) TabbedActivity.getViewPager().setCurrentItem(SECCION_DESCARGADAS);
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

