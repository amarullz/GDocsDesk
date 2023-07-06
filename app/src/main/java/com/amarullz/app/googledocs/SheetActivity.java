package com.amarullz.app.googledocs;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class SheetActivity extends AppCompatActivity {
  private DocsView doc;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    doc=new DocsView(this,"https://docs.google.com/spreadsheets/");
  }
}