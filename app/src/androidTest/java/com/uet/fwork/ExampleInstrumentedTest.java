package com.uet.fwork;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import com.google.firebase.database.FirebaseDatabase;
import com.uet.fwork.database.model.AddressModel;
import com.uet.fwork.database.model.CandidateModel;
import com.uet.fwork.database.model.EmployerModel;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.uet.fwork", appContext.getPackageName());

        EmployerModel employerModel = new EmployerModel(
            "1", "nguyendanghoangdao", "skakls", "sjkas",
            "alskaskl", "ạkjasjkas",  new AddressModel(
                    "Hai Duong", "KimThanh", "Dong Cam", "Doi 7"
        )
        );

        FirebaseDatabase.getInstance().getReference()
                .child("users").child("2").setValue(employerModel);
    }
}