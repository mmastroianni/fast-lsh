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

import org.apache.commons.cli.Option;

public class RequiredOption extends Option
{
    private static final long serialVersionUID = -7080065865178012224L;

    public RequiredOption(String opt, boolean hasArg, String description)
    {
        super(opt, hasArg, description);
        setRequired(true);
    }
    
    public RequiredOption(String opt, String longOpt, boolean hasArg, String description)
    {
        super(opt, longOpt, hasArg, description);
        setRequired(true);
    }
    
}