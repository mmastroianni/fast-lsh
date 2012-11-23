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

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ResourcePool<R> {

    private AtomicBoolean open_ = new AtomicBoolean(false);

    Queue<R> availableQ_ = new LinkedList<R>();
    Queue<R> inUseQ_ = new LinkedList<R>();

    private ReentrantLock acquireLock_ = new ReentrantLock();
    private Condition availableCond_ = acquireLock_.newCondition();
    private AtomicBoolean haveAvailable_ = new AtomicBoolean(false);

    /**
     * Open the pool for use
     */
    public void open()
    {
        open_.set(true);            
    }
    public boolean isOpen(){return open_.get();}

    /**
     * Are there any resources available? mostly helpful for testing
     * @return true if there are resources available for acquisition
     */
    public boolean haveAvailable()
    {
        return haveAvailable_.get();
    }
    
    /**
     * Close, but wait until no resources are outstanding
     * @throws InterruptedException
     */
    public void close() throws InterruptedException
    {
        if(!open_.get()){return;}
        try
        {
            acquireLock_.lock();
            while(!inUseQ_.isEmpty())
            {
                availableCond_.await();
            }
            open_.set(false);
        }
        finally
        {
            acquireLock_.unlock();          
        }
    }
    
    /**
     * Simply set the open flag to false
     * @throws InterruptedException
     */
    public void closeNow() throws InterruptedException
    {
        open_.set(false);
    }
    
    /**
     * Policy class for waiting on conditions
     *
     */
    private interface Awaiter
    {
        void await() throws InterruptedException;
    }
    
    /**
     * Utility method that allows use of Awaiter policy for waiting 
     * on condition if no resources are available
     * @param waiter policy for waiting until resources are available or timeout
     * @return resource, which may be null
     * @throws InterruptedException
     */
    private R acquire(Awaiter waiter) throws InterruptedException
    {
        try
        {
            acquireLock_.lock();
            if(!isOpen())
            {
                return null;
            }
            waiter.await();
            R ret = availableQ_.poll();
            if(ret != null)
            {
                inUseQ_.add(ret);
                if(availableQ_.isEmpty())
                {
                    haveAvailable_.set(false);
                }
            }
            return ret;
        }
        finally
        {
            acquireLock_.unlock();
        }
    }

    /**
     * Acquire a resource, waiting until one is free, with no timeout
     * use at own risk
     * @return a resource from the pool
     * @throws InterruptedException
     */
    public R acquire() throws InterruptedException
    {
        return acquire(new Awaiter(){public void await() throws InterruptedException
            {
                while(!haveAvailable_.get())
                {
                    availableCond_.await();
                }
            }
        }
        );
    }

    /**
     * Acquire a resource, but only wait for a specified time
     * @param timeout length of wait
     * @param timeUnit unit, as specified by java.util.concurrent.TimeUnit
     * @return a resource which may be null if timeout occurred
     * @throws InterruptedException
     */
    public R acquire(long timeout, java.util.concurrent.TimeUnit timeUnit) throws InterruptedException
    {
        return acquire(new TimedWaiter(timeout, timeUnit));
    }

    /**
     * Utility class for waiting a set period on a condition variable, dealing with 
     * spurious wakeups
     *
     */
    public class TimedWaiter implements Awaiter
    {
        long timeoutMillis_;
        public TimedWaiter(long timeout, java.util.concurrent.TimeUnit timeUnit)
        {
            timeoutMillis_ = timeUnit.toMillis(timeout);
        }
        
        @Override
        public void await() throws InterruptedException {
            try
            {
                long beginning = System.currentTimeMillis();
                while(!haveAvailable_.get())
                {
                    availableCond_.await(timeoutMillis_, java.util.concurrent.TimeUnit.MILLISECONDS);
                    long elapsed = System.currentTimeMillis() - beginning;
                    if(elapsed > timeoutMillis_){break;}
                }                                                   
            }
            catch(Exception e)
            {
                //TODO: logging
            }
        }
    }
    
    /**
     * Release a resource you have acquired
     * @param resource
     */
    public void release(R resource)
    {
        try
        {
            acquireLock_.lock();
            if(inUseQ_.contains(resource))
            {
                inUseQ_.remove(resource);
                availableQ_.add(resource);
                haveAvailable_.set(true);
                availableCond_.signal();                                        
            }
        }
        finally
        {
            acquireLock_.unlock();          
        }
    }

    /**
     * Add a resource to the queue. Threadsafe, and
     * makes resource immediately available
     * @param resource to be added
     * @return true if the availableq was modified (it
     * will not be if the item is already present).
     */
    public boolean add(R resource)
    {
        try
        {
            acquireLock_.lock();
            boolean retval = availableQ_.add(resource);
            if(retval)
            {
                haveAvailable_.set(true);
                availableCond_.signal();                                                    
            }
            return retval;
        }
        finally
        {
            acquireLock_.unlock();
        }
    }
    
    /**
     * Remove a resource: if it is still in use,  block until it is released
     * @param resource
     * @return true if this resource was removed, false else
     * @throws InterruptedException
     */
    public boolean remove(R resource) throws InterruptedException
    {
        try
        {
            acquireLock_.lock();
            boolean retval = availableQ_.remove(resource); //returns false if it is in use, and hence in the other queue
            while(!retval)
            {
                while(!haveAvailable_.get()) availableCond_.await();
                retval = availableQ_.remove(resource);
            }
            if(availableQ_.isEmpty())haveAvailable_.set(false);
            return retval;
        }
        finally
        {
            acquireLock_.unlock();
        }
    }
    
    /**
     * Remove a resource from the queue regardless of whether 
     * we have other resources in use
     * @param resource to remove
     * @return true if this resource was removed, false else
     */
    public boolean removeNow(R resource)
    {
        try
        {
            acquireLock_.lock();
            boolean retval = availableQ_.remove(resource);          
            if(!retval) retval = inUseQ_.remove(resource);
            else if(availableQ_.isEmpty()) haveAvailable_.set(false);
            return retval;
        }
        finally
        {
            acquireLock_.unlock();
        }
        
    }
}
