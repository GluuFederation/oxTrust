/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.model.scim2.provider;

import java.io.Serializable;

public class PatchConfig implements Serializable {

  private final boolean supported;

  /**
   * Create a <code>PatchConfig</code> instance.
   *
   * @param supported  Specifies whether the PATCH operation is supported.
   */
  public PatchConfig(final boolean supported)
  {
    this.supported = supported;
  }

  /**
   * Indicates whether the PATCH operation is supported.
   * @return  {@code true} if the PATCH operation is supported.
   */
  public boolean isSupported()
  {
    return supported;
  }

}
