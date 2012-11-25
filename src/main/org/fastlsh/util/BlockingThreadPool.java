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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class BlockingThreadPool
{
    protected static class SemaphoreRunnable implements Runnable
    {
        final private Semaphore flag_;
        final private Runnable runnable_;
        public SemaphoreRunnable(Semaphore f, Runnable r)
        {
            flag_ = f;
            runnable_ = r;
        }
        
        @Override
        public void run()
        {
            try
            {
                runnable_.run();
            }
            finally
            {
                flag_.release();
            }
            
        }
        
    }
    
    protected ExecutorService pool;
    protected Semaphore flag;
    
    public BlockingThreadPool(int numThreads, int queueSize)
    {
        flag = new Semaphore(queueSize);
        pool = Executors.newFixedThreadPool(numThreads);
    }
    
    public void execute(Runnable task) {
        boolean acquired = false;
        do 
        {
            try 
            {
                flag.acquire();
                acquired = true;
            } 
            catch (InterruptedException e) {}                   
        } while(!acquired);
        pool.execute(new SemaphoreRunnable(flag, task));
    }
        
    public void awaitTermination(long timeout, TimeUnit unit) throws InterruptedException
    {        
    	pool.shutdown();
    	pool.awaitTermination(timeout, unit);
    }
}
