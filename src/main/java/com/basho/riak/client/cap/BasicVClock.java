/*
 * This file is provided to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.basho.riak.client.cap;

import com.basho.riak.client.util.CharsetUtils;
import java.util.Arrays;

/**
 * An implementation of {@link VClock} that wraps a <code>byte[]</code>
 * @author russell
 * 
 */
public class BasicVClock implements VClock {

    private final byte[] value;

    /**
     * Create BasicVclock
     * @param value the vector clock bytes. NOTE: clones the value
     * @throws IllegalArgumentException if <code>value</code> is null
     */
    public BasicVClock(final byte[] value) {
        if (value == null) {
            throw new IllegalArgumentException("VClock value cannot be null");
        }
        this.value = value.clone();
    }

    /**
     * @return a copy of this vector clocks bytes
     */
    public byte[] getBytes() {
        return value.clone();
    }

    /**
     * @return a UTF-8 String of this vector clocks bytes
     */
    public String asString() {
        return CharsetUtils.asUTF8String(value);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (!(obj instanceof BasicVClock)) {
            return false;
        } 
        
        BasicVClock bvc = (BasicVClock)obj;
        return Arrays.equals(value, bvc.value);
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 97 * hash + Arrays.hashCode(this.value);
        return hash;
    }
    
}
