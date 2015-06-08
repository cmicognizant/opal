yumrepo { "cmi":
  #baseurl   => "http://translationeelonderzoek.cmi-vzw.be/nexus/content/groups/public",
  baseurl   => "http://translationeelonderzoek.cmi-vzw.be/nexus/repositories/releases",
  descr     => "CMI Releases Repository",
  enabled   => 1,
  gpgcheck  => 0
}

package { 'epel-release':
  ensure    => present,
  provider  => rpm,
  source    => 'http://dl.fedoraproject.org/pub/epel/6/x86_64/epel-release-6-8.noarch.rpm',
}
