package edu.usc.cs.nsl.lookingglass.service;

import edu.usc.cs.nsl.lookingglass.database.DBManager;
import edu.usc.cs.nsl.lookingglass.tracert.HttpQuery;
import edu.usc.cs.nsl.lookingglass.tracert.LGManager;
import edu.usc.cs.nsl.lookingglass.tracert.Query;
import edu.usc.cs.nsl.lookingglass.tracert.TelnetQuery;
import java.util.LinkedList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import static org.junit.Assert.*;

/**
 *
 * @author matt
 */
public class TracerouteServiceImplTest {
    
    public TracerouteServiceImplTest() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of submit method, of class TracerouteServiceImpl.
     */
    @Test
    public void testSubmit() throws Exception {
        
        System.out.println("submit");
        
        QueryProcessor queryProcessor = Mockito.mock(QueryProcessor.class);
        DBManager dbManager = Mockito.mock(DBManager.class);
        Query query = Mockito.mock(Query.class);
        HttpQuery httpQuery = Mockito.mock(HttpQuery.class);
        TelnetQuery telnetQuery = Mockito.mock(TelnetQuery.class);
        
        Mockito.when(dbManager.createQueryFromServer("Comcast")).thenReturn(query);
        Mockito.when(dbManager.createHttpQueryFromServer("Comcast")).thenReturn(httpQuery);
        Mockito.when(dbManager.createTelnetQueryFromServer("Comcast")).thenReturn(telnetQuery);
        Mockito.when(dbManager.createQueryFromServer("Foobar")).thenReturn(null);
        
        final TracerouteServiceImpl instance = new TracerouteServiceImpl(dbManager, queryProcessor);
        
        new Thread() {
            @Override
            public void run() {
                try { 
                    instance.start();
                } catch(Exception e){ System.err.println(e); }
            }
        }.start();
        
        Thread.sleep(1000); //let worker thread get started
         
        Request request1 = new Request("Comcast", "www.google.com", null);
        boolean result1 = instance.submit(request1);
        assertTrue(result1);
        Mockito.verify(dbManager).createQueryFromServer("Comcast");
        Mockito.verify(queryProcessor).submit(query);
        
        Request request2 = new Request("Comcast", "www.google.com", "http");
        boolean result2 = instance.submit(request2);
        assertTrue(result2);
        Mockito.verify(dbManager).createHttpQueryFromServer("Comcast");
        Mockito.verify(queryProcessor).submit(httpQuery);
        
        Request request3 = new Request("Comcast", "www.google.com", "telnet");
        boolean result3 = instance.submit(request3);
        assertTrue(result3);
        Mockito.verify(dbManager).createTelnetQueryFromServer("Comcast");
        Mockito.verify(queryProcessor).submit(telnetQuery);
        
        Mockito.reset(queryProcessor, dbManager);
        
        Request request4 = new Request("Comcast", "www.google.com", "foobar");
        boolean result4 = instance.submit(request4);
        assertFalse(result4);
        Mockito.verifyZeroInteractions(dbManager, queryProcessor);
        
        instance.stop();
    }
}