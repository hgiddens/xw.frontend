package xw.frontend
package resources.config

import org.specs2.mutable.Specification

object ResourceConfigSpec extends Specification {
  val dir = "resource-config-spec"
  val md5 = "615afc21019ae578d7b35480769e0e94"
  val base = s"digest-file"
  val example = s"$dir/$md5-$base"
  val nonDigest = s"$dir/non-digest-file"
  val absent = s"$dir/absent-file"

  "digestFrom" should {
    "extract the digest from the simple case" in {
      val Right(config) = ResourceConfig()
      config.digestFrom(example) must beSome(md5)
    }

    "extract the digest at the root" in {
      val Right(config) = ResourceConfig()
      config.digestFrom(s"$md5-$dir-$base") must beSome(md5)
    }

    "not identify obviously non-digest things" in {
      val Right(config) = ResourceConfig()
      config.digestFrom(base) must beNone
    }
  }

  "digest.asset" should {
    "find the base path for non-digest assets" in {
      val Right(config) = ResourceConfig()
      config.publicAssetPath(nonDigest) must_=== s"/${config.staticRoot}/$nonDigest"
    }

    "find the base path for missing assets" in {
      val Right(config) = ResourceConfig()
      config.publicAssetPath(absent) must_=== s"/${config.staticRoot}/$absent"
    }

    "find the prefix path for digest assets" in {
      val Right(config) = ResourceConfig()
      config.publicAssetPath(s"$dir/$base") must_=== s"/${config.staticRoot}/$example"
    }

    "find the prefix path for digest assets at the root" in {
      val Right(config) = ResourceConfig()
      config.publicAssetPath(s"$dir-$base") must_=== s"/${config.staticRoot}/$md5-$dir-$base"
    }
  }

  "digest.exist" should {
    "be false for missing assets" in {
      val Right(config) = ResourceConfig()
      config.assetExists(absent) must beFalse
    }

    "be true for digest assets" in {
      val Right(config) = ResourceConfig()
      config.assetExists(s"$dir/$base") must beTrue
    }

    "be true for digest assets at the root" in {
      val Right(config) = ResourceConfig()
      config.assetExists(s"$dir-$base") must beTrue
    }
  }
}
