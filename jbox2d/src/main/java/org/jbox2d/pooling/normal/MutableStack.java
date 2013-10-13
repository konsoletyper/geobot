/*******************************************************************************
 * Copyright (c) 2013, Daniel Murphy
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 	* Redistributions of source code must retain the above copyright notice,
 * 	  this list of conditions and the following disclaimer.
 * 	* Redistributions in binary form must reproduce the above copyright notice,
 * 	  this list of conditions and the following disclaimer in the documentation
 * 	  and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package org.jbox2d.pooling.normal;

import org.jbox2d.pooling.IDynamicStack;

public abstract class MutableStack<E> implements IDynamicStack<E> {
    private Object[] stack;
    private int index;

    public MutableStack(int argInitSize) {
        stack = new Object[argInitSize];
    }

    private void extendStack(int argSize) {
        Object[] newStack = new Object[argSize];
        System.arraycopy(stack, 0, newStack, 0, stack.length);
        stack = newStack;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jbox2d.pooling.IDynamicStack#pop()
     */
    @Override
    @SuppressWarnings("unchecked")
    public final E pop() {
        if (index == 0) {
            return createElement();
        }
        return (E)stack[--index];
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jbox2d.pooling.IDynamicStack#push(E)
     */
    @Override
    public final void push(E argObject) {
        stack[index] = argObject;
        if (index == stack.length) {
            extendStack(stack.length * 2);
        }
    }

    protected abstract E createElement() ;
}
