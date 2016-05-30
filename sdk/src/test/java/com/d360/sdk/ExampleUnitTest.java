package com.d360.sdk;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void sendMessage() throws Exception {
        D360SDK.get().sendEvent("coucou", null);
    }
}