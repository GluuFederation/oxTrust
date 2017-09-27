/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.model.scim2.provider;

import org.gluu.oxtrust.model.scim2.annotations.Attribute;
import org.gluu.oxtrust.model.scim2.AttributeDefinition;

public class PatchConfig{

    @Attribute(description = "A Boolean value specifying whether or not the operation is supported.",
            isRequired = true,
            mutability = AttributeDefinition.Mutability.READ_ONLY)
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
