package org.gluu.oxtrust.model.scim2.provider;

public class PatchConfig
{
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
