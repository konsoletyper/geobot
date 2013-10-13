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
/**
 * Created at 12:52:04 AM Jan 20, 2011
 */
package org.jbox2d.pooling.normal;

/**
 * @author Daniel Murphy
 */
public abstract class OrderedStack<E> {
  private final Object[] pool;
  private int index;

  public OrderedStack(int argStackSize) {
    pool = new Object[argStackSize];
    index = 0;
  }

  public final E pop() {
    @SuppressWarnings("unchecked")
    E result = (E)pool[index];
    if (result == null) {
        result = createElement();
        pool[index] = result;
    }
    ++index;
    return result;
  }

  public final E[] pop(int argNum) {
    E[] container = createArray(argNum);
    for (int i = 0; i < argNum; ++i) {
        @SuppressWarnings("unchecked")
        E elem = (E)pool[index];
        if (elem == null) {
            elem = createElement();
            pool[index] = elem;
        }
        container[i] = elem;
    }
    index += argNum;
    return container;
  }

  public final void push(int argNum) {
    index -= argNum;
  }

  protected abstract E createElement();

  protected abstract E[] createArray(int size);
}
