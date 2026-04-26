package com.servalabs.chat.registration.secondary

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test
import org.signal.libsignal.protocol.IdentityKeyPair
import com.servalabs.chat.devicelist.protos.DeviceName
import java.nio.charset.Charset

class DeviceNameCipherTest {
  @Test
  fun encryptDeviceName() {
    val deviceName = "xXxCoolDeviceNamexXx"
    val identityKeyPair = IdentityKeyPair.generate()

    val encryptedDeviceName = DeviceNameCipher.encryptDeviceName(deviceName.toByteArray(Charset.forName("UTF-8")), identityKeyPair)

    val plaintext = DeviceNameCipher.decryptDeviceName(DeviceName.ADAPTER.decode(encryptedDeviceName), identityKeyPair)!!

    assertThat(String(plaintext, Charset.forName("UTF-8"))).isEqualTo(deviceName)
  }
}
