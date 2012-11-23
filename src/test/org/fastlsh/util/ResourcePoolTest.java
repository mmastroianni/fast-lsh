/*
   Copyright 2012 Michael Mastroianni, Amol Kapila, Ryan Berdeen (fastlsh.org)
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.fastlsh.util;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.AfterClass;

public class ResourcePoolTest 
{
    private static ExecutorService threadpool_ = Executors.newFixedThreadPool(5);

    @AfterClass
    public static void  cleanup()
    {
        threadpool_.shutdown();
    }
    
    public static class TestResource
    {
        public static AtomicInteger testCount_ = new AtomicInteger(0);
        
        public static void resetCount()
        {
            testCount_.set(0);
        }
        
        public static int getCount()
        {
            return testCount_.get();
        }
        
        public void incrementCount()
        {
            testCount_.incrementAndGet();
        }               
    }

    private class TimedResourceAdder implements Runnable
    {
        private final int waitMillis_;
        private final ResourcePool<TestResource> pool_;
        @SuppressWarnings("unused")
        public boolean succeeded_ = false;
        public TimedResourceAdder(int waitMillis, ResourcePool<TestResource> pool)
        {
            waitMillis_ = waitMillis;
            pool_ = pool;
        }

        @Override
        public void run() 
        {
            try 
            {   
                Thread.sleep(waitMillis_);
                succeeded_ = pool_.add(new TestResource());
            } catch (Exception e)//InterruptedException e) 
            {
                // TODO logging
            }
        }       
    }

    private class Closer implements Runnable
    {
        ResourcePool<TestResource> pool_;
        
        public Closer(ResourcePool<TestResource> pool)
        {
            pool_ = pool;
        }
        
        @Override
        public void run() 
        {
            try
            {
                pool_.close();
            }
            catch(Exception e)
            {
                return;
            }
        }       
    }

    private class TimedAcquirer implements Runnable
    {
        final private ResourcePool<TestResource> pool_;
        final private long timeoutMillis_;
        TestResource tr_= null;
        
        public boolean resourceIsNull()
        {
            return tr_ == null;
        }
        
        //Set timeout to -1 for infinite wait
        public TimedAcquirer(ResourcePool<TestResource> p, long timeoutMillis)
        {
            pool_ = p;
            timeoutMillis_ = timeoutMillis;
        }
        
        @Override
        public void run() 
        {
            try 
            {
                if(timeoutMillis_ == -1)
                {
                    tr_ = pool_.acquire();
                }
                else
                {
                    tr_ = pool_.acquire(timeoutMillis_, TimeUnit.MILLISECONDS);             
                }
            } 
            catch (InterruptedException e) 
            {
                throw(new RuntimeException(e));
            }
        }
        
    }
    @Test
    public void testOpen() throws InterruptedException
    {
        ResourcePool<TestResource> pool = new ResourcePool<TestResource>();
        TestResource tr = pool.acquire();
        Assert.assertNull(tr);
        pool.add(new TestResource());
        tr = pool.acquire();
        Assert.assertNull(tr);
        pool.open();
        tr = pool.acquire();
        Assert.assertNotNull(tr);
        Assert.assertTrue(true);
    }

    @Test
    public void testClose() throws InterruptedException
    {
        ResourcePool<TestResource> pool = new ResourcePool<TestResource>();
        pool.add(new TestResource());
        pool.add(new TestResource());
        pool.open();
        TestResource tr1 = pool.acquire();
        Assert.assertNotNull(tr1);      
        threadpool_.execute(new Closer(pool));
        //Give it some time for execution to have started
        Thread.sleep(100);
        //make sure close hasn't completed since we didn't release tr1
        TestResource tr2 = pool.acquire();
        Assert.assertNotNull(tr2);      
        pool.release(tr1);      
        
        Thread.sleep(50);
        //make sure close hasn't completed since we didn't release tr2
        TestResource tr3 = pool.acquire();
        Assert.assertNotNull(tr3);      

        pool.release(tr2);
        pool.release(tr3);
        Thread.sleep(100);
        //now that we've released everything we've acquired, close should complete
        tr3 = pool.acquire();
        Assert.assertNull(tr3);
    }
        
    @Test
    public void testTimedAcquire() throws InterruptedException
    {
        ResourcePool<TestResource> pool = new ResourcePool<TestResource>();
        pool.open();
        //Check that we wait the right amount of time and get back null on an
        //empty pool
        long start = System.currentTimeMillis();
        TestResource tr1 = pool.acquire(100000, TimeUnit.MICROSECONDS);
        long  end = System.currentTimeMillis();
        long elapsed = end - start;
        Assert.assertNull(tr1);
        Assert.assertTrue(elapsed < 250);
        
        //Check that we get back null if something is added too late after we started
        //waiting
        TimedResourceAdder adder = new TimedResourceAdder(500, pool);
        threadpool_.execute(adder);
        tr1 = pool.acquire(100, TimeUnit.MILLISECONDS);
        Assert.assertNull(tr1);

        //Empty the pool back out
        tr1 = pool.acquire();
        Assert.assertNotNull(tr1);
        pool.release(tr1);
        pool.remove(tr1);
        //Check that we come back with non null if something is added while we are waiting
        //and before our time limit
        adder = new TimedResourceAdder(100, pool);
        threadpool_.execute(adder);
        tr1 = pool.acquire(300, TimeUnit.MILLISECONDS);
        Assert.assertNotNull(tr1);
    }
    
    @Test
    public void testRelease() throws InterruptedException
    {
        ResourcePool<TestResource> pool = new ResourcePool<TestResource>();
        pool.open();
        ArrayList<TimedAcquirer> acquirers = new ArrayList<TimedAcquirer>();
        TestResource tr1 = new TestResource();
        pool.add(tr1);
        //make the first one unavailable, so we can test releasing it to make 
        //all the acquirers happy
        int numAcquirers = 5;       
        for(int i = 0; i < numAcquirers; ++i)
        {
            TimedAcquirer ta = new TimedAcquirer(pool, 1500);
            acquirers.add(ta);
            threadpool_.execute(ta);
        }
        Thread.sleep(100);
        //this should satisfy all but one acquirer
        for(int i = 0; i < (numAcquirers -1); ++i)
        {
            TestResource tr = new TestResource();
            pool.add(tr);
        }
        Thread.sleep(100);
        //this should satisfy the last acquirer
        pool.release(tr1);
        Thread.sleep(100);
        for (TimedAcquirer timedAcquirer : acquirers) 
        {
            Assert.assertFalse(timedAcquirer.resourceIsNull());
        }
    }
    
    private class TimedReleaser implements Runnable
    {
        final private long timeout_;
        final private ResourcePool<TestResource> pool_;
        final private TestResource toRelease_;
        
        public TimedReleaser(long timeout, ResourcePool<TestResource> pool, TestResource toRelease)
        {
            timeout_ = timeout;
            pool_ = pool;
            toRelease_ = toRelease;
        }
        @Override
        public void run() 
        {
            try 
            {
                Thread.sleep(timeout_);
                pool_.release(toRelease_);
            } 
            catch (InterruptedException e) 
            {
            }           
        }
        
    }
    
    @Test 
    public void testRemove() throws InterruptedException
    {
        ResourcePool<TestResource> pool = new ResourcePool<TestResource>();
        pool.open();
        TestResource tr = new TestResource();
        pool.add(tr);       
        tr = pool.acquire();
        TimedReleaser releaser = new TimedReleaser(200, pool, tr);
        threadpool_.execute(releaser);
        long start = System.currentTimeMillis();
        pool.remove(tr);
        long elapsed = System.currentTimeMillis() - start;
        Assert.assertTrue(elapsed > 200);
        Assert.assertTrue(pool.availableQ_.isEmpty());
        Assert.assertTrue(pool.inUseQ_.isEmpty());      
    }
    
    @Test 
    public void testRemoveNow() throws InterruptedException
    {
        ResourcePool<TestResource> pool = new ResourcePool<TestResource>();
        pool.open();
        TestResource tr = new TestResource();
        pool.add(tr);       
        tr = pool.acquire();
        long start = System.currentTimeMillis();
        pool.removeNow(tr);
        long elapsed = System.currentTimeMillis() - start;
        Assert.assertTrue(elapsed < 100);
        Assert.assertTrue(pool.availableQ_.isEmpty());
        Assert.assertTrue(pool.inUseQ_.isEmpty());              
    }
    
    @Test 
    public void testCloseNow() throws InterruptedException
    {
        ResourcePool<TestResource> pool = new ResourcePool<TestResource>();
        pool.open();
        pool.add(new TestResource());       
        pool.add(new TestResource());
        TestResource tr1 = pool.acquire();
        Assert.assertNotNull(tr1);
        long start = System.currentTimeMillis();
        pool.closeNow();
        long elapsed = System.currentTimeMillis() - start;
        Assert.assertTrue(elapsed < 100);
        Assert.assertFalse(pool.isOpen());
        TestResource tr2 = pool.acquire();
        Assert.assertNull(tr2);
    }

}
