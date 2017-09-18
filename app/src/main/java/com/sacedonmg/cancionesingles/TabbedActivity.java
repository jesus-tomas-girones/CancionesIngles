package com.sacedonmg.cancionesingles;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static com.sacedonmg.cancionesingles.UtilidadesCanciones.sincroListReproduccion;

public class TabbedActivity extends AppCompatActivity {

    public static int ACTIVIDAD_VISTA_CANCION_LOCAL = 4567;
    public static int ACTIVIDAD_VISTA_CANCION_REMOTA = 4568;
    public static int ACTIVIDAD_EDICION = 5678;

    public static final int SECCION_DESCARGADAS = 0;
    public static final int SECCION_REMOTAS = 1;

    private SectionsPagerAdapter mSectionsPagerAdapter;
        private final int SECTIONS[] = {
            R.string.section_downloaded,
            R.string.section_availables,
    };

    private static ViewPager mViewPager;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sincroListReproduccion();
                ListaCanciones.adaptador.notifyDataSetChanged();
                //ListaCanciones.adaptador.notifyItemRangeChanged(0, ListaCanciones.vectorCanciones.tamanyo());
                //mViewPager.setCurrentItem(tab.getPosition());
            }
        });

        context = this;
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
                        startActivityForResult(i, ACTIVIDAD_EDICION);
                    }

                })
                .setNegativeButton(R.string.cancelar,null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            lanzarPreferencias();
            return true;
        }
        if(id == R.id.nuevo){
            lanzarNuevo();
            return true;
        }
        if (id == R.id.acercaDe){
            lanzarAcercaDe();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_tabbed, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case SECCION_DESCARGADAS:
                    return ListaCanciones.newInstance();
                case SECCION_REMOTAS:
                    return ListaCancionesRemoto.newInstance();
                default:
                    return PlaceholderFragment.newInstance(position + 1);
            }
        }

        @Override
        public int getCount() {
            return SECTIONS.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position < SECTIONS.length) {
                return getString(SECTIONS[position]);
            }

            return null;
        }
    }

    @Override
    public void onBackPressed(){
        finish();
    }

    public static ViewPager getViewPager() {
        return mViewPager;
    }
}
