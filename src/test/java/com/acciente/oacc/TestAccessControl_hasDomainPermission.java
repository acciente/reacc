/*
 * Copyright 2009-2015, Acciente LLC
 *
 * Acciente LLC licenses this file to you under the
 * Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.acciente.oacc;

import org.junit.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class TestAccessControl_hasDomainPermission extends TestAccessControlBase {
   @Test
   public void hasDomainPermission_succeedsAsSystemResource() {
      authenticateSystemResource();

      final String domainName = generateDomain();
      final Resource accessorResource = generateUnauthenticatableResource();

      final Set<DomainPermission> allDomainPermissions
            = accessControlContext.getEffectiveDomainPermissions(SYS_RESOURCE, domainName);

      assertThat(allDomainPermissions.size(), is(2));

      // verify
      if (!accessControlContext.hasDomainPermission(SYS_RESOURCE,
                                                    DomainPermissions.getInstance(DomainPermissions.SUPER_USER),
                                                    domainName)) {
         fail("checking SUPER_USER domain permission should have succeeded for system resource");
      }
      if (!accessControlContext.hasDomainPermission(SYS_RESOURCE,
                                                    DomainPermissions.getInstance(DomainPermissions.SUPER_USER, true),
                                                    domainName)) {
         fail("checking SUPER_USER /G domain permission should have succeeded for system resource");
      }
      if (!accessControlContext.hasDomainPermission(SYS_RESOURCE,
                                                    DomainPermissions.getInstance(DomainPermissions.CREATE_CHILD_DOMAIN),
                                                    domainName)) {
         fail("checking CREATE_CHILD_DOMAIN domain permission should have succeeded for system resource");
      }
      if (!accessControlContext.hasDomainPermission(SYS_RESOURCE,
                                                    DomainPermissions.getInstance(DomainPermissions.CREATE_CHILD_DOMAIN, true),
                                                    domainName)) {
         fail("checking CREATE_CHILD_DOMAIN /G domain permission should have succeeded for system resource");
      }

      if (accessControlContext.hasDomainPermission(accessorResource,
                                                   DomainPermissions.getInstance(DomainPermissions.SUPER_USER),
                                                   domainName)) {
         fail("checking domain permission for accessor resource when none exist should have failed");
      }
      if (accessControlContext.hasDomainPermission(accessorResource,
                                                   DomainPermissions.getInstance(DomainPermissions.CREATE_CHILD_DOMAIN),
                                                   domainName)) {
         fail("checking domain permission for accessor resource when none exist should have failed");
      }
   }

   @Test
   public void hasDomainPermission_emptyAsAuthenticated() {
      final Resource accessorResource = generateUnauthenticatableResource();

      final String domainName = generateDomain();
      generateResourceAndAuthenticate();

      final Map<String,Set<DomainPermission>> allDomainPermissions 
            = accessControlContext.getEffectiveDomainPermissionsMap(accessorResource);
      assertThat(allDomainPermissions.isEmpty(), is(true));

      // verify
      if (accessControlContext.hasDomainPermission(accessorResource,
                                                   DomainPermissions.getInstance(DomainPermissions.SUPER_USER),
                                                   domainName)) {
         fail("checking domain permission for authenticated accessor resource when none exist should have failed");
      }
      if (accessControlContext.hasDomainPermission(accessorResource,
                                                    DomainPermissions.getInstance(DomainPermissions.CREATE_CHILD_DOMAIN),
                                                    domainName)) {
         fail("checking domain permission for authenticated accessor resource when none exist should have failed");
      }
   }

   @Test
   public void hasDomainPermission_validAsSystemResource() {
      authenticateSystemResource();
      final DomainPermission domPerm_superuser
            = DomainPermissions.getInstance(DomainPermissions.SUPER_USER);
      final DomainPermission domPerm_child
            = DomainPermissions.getInstance(DomainPermissions.CREATE_CHILD_DOMAIN);
      final DomainPermission domPerm_child_withGrant
            = DomainPermissions.getInstance(DomainPermissions.CREATE_CHILD_DOMAIN, true);

      final String domainName1 = generateDomain();
      final String domainName2 = generateDomain();

      // set domain permissions
      Resource accessorResource = generateUnauthenticatableResource();
      Set<DomainPermission> domainPermissions_pre1 = setOf(domPerm_superuser, domPerm_child);
      accessControlContext.setDomainPermissions(accessorResource, domainName1, domainPermissions_pre1);

      // get domain create permissions and verify
      final Map<String,Set<DomainPermission>> allDomainPermissions
            = accessControlContext.getEffectiveDomainPermissionsMap(accessorResource);
      assertThat(allDomainPermissions.size(), is(1));
      assertThat(allDomainPermissions.get(domainName1), is(domainPermissions_pre1));

      if (!accessControlContext.hasDomainPermission(accessorResource, domPerm_superuser, domainName1)) {
         fail("checking valid domain permission for system resource should have succeeded");
      }
      if (!accessControlContext.hasDomainPermission(accessorResource, domPerm_child, domainName1)) {
         fail("checking valid domain permission for system resource should have succeeded");
      }

      // let's try another domain
      Set<DomainPermission> domainPermissions_pre2 = setOf(domPerm_child_withGrant);
      accessControlContext.setDomainPermissions(accessorResource, domainName2, domainPermissions_pre2);

      // get domain create permissions and verify
      final Map<String,Set<DomainPermission>> allDomainPermissions2
            = accessControlContext.getEffectiveDomainPermissionsMap(accessorResource);
      assertThat(allDomainPermissions2.size(), is(2));
      assertThat(allDomainPermissions2.get(domainName1), is(domainPermissions_pre1));
      assertThat(allDomainPermissions2.get(domainName2), is(domainPermissions_pre2));

      if (!accessControlContext.hasDomainPermission(accessorResource, domPerm_child_withGrant, domainName2)) {
         fail("checking valid domain permissions for system resource should have succeeded");
      }
   }

   @Test
   public void hasDomainPermission_validWithInheritFromParentDomain() {
      authenticateSystemResource();
      final DomainPermission domPerm_superuser
            = DomainPermissions.getInstance(DomainPermissions.SUPER_USER);
      final DomainPermission domPerm_superuser_withGrant
            = DomainPermissions.getInstance(DomainPermissions.SUPER_USER, true);
      final DomainPermission domPerm_createchilddomain
            = DomainPermissions.getInstance(DomainPermissions.CREATE_CHILD_DOMAIN);
      final DomainPermission domPerm_createchilddomain_withGrant
            = DomainPermissions.getInstance(DomainPermissions.CREATE_CHILD_DOMAIN, true);

      final String childDomain = generateUniqueDomainName();
      final String parentDomain = generateDomain();
      accessControlContext.createDomain(childDomain, parentDomain);

      // set parent domain create permissions
      Resource accessorResource = generateUnauthenticatableResource();
      Set<DomainPermission> parentDomainPermissions_pre = setOf(domPerm_superuser_withGrant, domPerm_createchilddomain);
      accessControlContext.setDomainPermissions(accessorResource, parentDomain, parentDomainPermissions_pre);

      // set child domain permissions
      Set<DomainPermission> childDomainPermissions_pre = setOf(domPerm_superuser, domPerm_createchilddomain_withGrant);
      accessControlContext.setDomainPermissions(accessorResource, childDomain, childDomainPermissions_pre);

      // verify
      if (!accessControlContext.hasDomainPermission(accessorResource, domPerm_superuser_withGrant, childDomain)) {
         fail("checking valid inherited domain permissions for system resource should have succeeded");
      }
      if (!accessControlContext.hasDomainPermission(accessorResource, domPerm_createchilddomain_withGrant, childDomain)) {
         fail("checking valid inherited domain permissions for system resource should have succeeded");
      }
   }

   @Test
   public void hasDomainPermission_validWithInheritFromAncestorDomainWithEmptyIntermediaryAncestors() {
      authenticateSystemResource();
      final DomainPermission domPerm_superuser
            = DomainPermissions.getInstance(DomainPermissions.SUPER_USER);
      final DomainPermission domPerm_superuser_withGrant
            = DomainPermissions.getInstance(DomainPermissions.SUPER_USER, true);
      final DomainPermission domPerm_createchilddomain
            = DomainPermissions.getInstance(DomainPermissions.CREATE_CHILD_DOMAIN);
      final DomainPermission domPerm_createchilddomain_withGrant
            = DomainPermissions.getInstance(DomainPermissions.CREATE_CHILD_DOMAIN, true);

      final String parentDomain = generateDomain();
      final String childDomain = generateUniqueDomainName();
      accessControlContext.createDomain(childDomain, parentDomain);
      final String grandChildDomain = generateUniqueDomainName();
      accessControlContext.createDomain(grandChildDomain, childDomain);
      final String greatGrandChildDomain = generateUniqueDomainName();
      accessControlContext.createDomain(greatGrandChildDomain, grandChildDomain);
      final String greatGreatGrandChildDomain = generateUniqueDomainName();
      accessControlContext.createDomain(greatGreatGrandChildDomain, greatGrandChildDomain);

      // set parent domain create permissions
      Resource accessorResource = generateUnauthenticatableResource();
      Set<DomainPermission> parentDomainPermissions_pre = setOf(domPerm_superuser_withGrant, domPerm_createchilddomain);
      accessControlContext.setDomainPermissions(accessorResource, parentDomain, parentDomainPermissions_pre);

      // set child domain permissions
      Set<DomainPermission> childDomainPermissions_pre = setOf(domPerm_superuser);
      accessControlContext.setDomainPermissions(accessorResource, childDomain, childDomainPermissions_pre);

      // set great-great-grand-child domain permissions
      Set<DomainPermission> greatGreatGrandChildDomainPermissions_pre = setOf(domPerm_createchilddomain_withGrant);
      accessControlContext.setDomainPermissions(accessorResource,
                                                greatGreatGrandChildDomain,
                                                greatGreatGrandChildDomainPermissions_pre);

      // verify
      if (!accessControlContext.hasDomainPermission(accessorResource,
                                                    domPerm_superuser_withGrant,
                                                    greatGreatGrandChildDomain)) {
         fail("checking valid domain permissions inherited from ancestor domain with empty intermediary should have succeeded");
      }
      if (!accessControlContext.hasDomainPermission(accessorResource,
                                                    domPerm_createchilddomain_withGrant,
                                                    greatGreatGrandChildDomain)) {
         fail("checking valid domain permissions inherited from ancestor domain with empty intermediary should have succeeded");
      }
   }

   @Test
   public void hasDomainPermission_validWithInheritFromResource() {
      authenticateSystemResource();
      final DomainPermission domPerm_superuser
            = DomainPermissions.getInstance(DomainPermissions.SUPER_USER);
      final DomainPermission domPerm_superuser_withGrant
            = DomainPermissions.getInstance(DomainPermissions.SUPER_USER, true);
      final DomainPermission domPerm_createchilddomain
            = DomainPermissions.getInstance(DomainPermissions.CREATE_CHILD_DOMAIN);
      final DomainPermission domPerm_createchilddomain_withGrant
            = DomainPermissions.getInstance(DomainPermissions.CREATE_CHILD_DOMAIN, true);

      final String domainName = generateDomain();
      Resource accessorResource = generateUnauthenticatableResource();

      // set child domain permissions
      Set<DomainPermission> directDomainPermissions_pre = new HashSet<>();
      directDomainPermissions_pre.add(domPerm_superuser_withGrant);
      directDomainPermissions_pre.add(domPerm_createchilddomain);
      accessControlContext.setDomainPermissions(accessorResource, domainName, directDomainPermissions_pre);

      // set donor permissions
      Resource donorResource = generateUnauthenticatableResource();
      Set<DomainPermission> donorDomainPermissions_pre = new HashSet<>();
      donorDomainPermissions_pre.add(domPerm_superuser);
      donorDomainPermissions_pre.add(domPerm_createchilddomain_withGrant);
      accessControlContext.setDomainPermissions(donorResource, domainName, donorDomainPermissions_pre);

      // set accessor --INHERIT-> donor
      Set<ResourcePermission> inheritanceResourcePermissions = setOf(ResourcePermissions.getInstance(ResourcePermissions.INHERIT));
      accessControlContext.setResourcePermissions(accessorResource, donorResource, inheritanceResourcePermissions);

      // verify
      if (!accessControlContext.hasDomainPermission(accessorResource, domPerm_superuser_withGrant, domainName)) {
         fail("checking valid domain permissions inherited from resource should have succeeded");
      }
      if (!accessControlContext.hasDomainPermission(accessorResource, domPerm_createchilddomain_withGrant, domainName)) {
         fail("checking valid domain permissions inherited from resource should have succeeded");
      }
   }

   @Test
   public void hasDomainPermission_validWithInheritFromAncestorDomainAndResource() {
      authenticateSystemResource();
      final DomainPermission domPerm_superuser_withGrant
            = DomainPermissions.getInstance(DomainPermissions.SUPER_USER, true);
      final DomainPermission domPerm_createchilddomain
            = DomainPermissions.getInstance(DomainPermissions.CREATE_CHILD_DOMAIN);
      final DomainPermission domPerm_createchilddomain_withGrant
            = DomainPermissions.getInstance(DomainPermissions.CREATE_CHILD_DOMAIN, true);

      final String childDomain = generateUniqueDomainName();
      final String parentDomain = generateDomain();
      accessControlContext.createDomain(childDomain, parentDomain);

      // set parent domain create permissions
      Resource accessorResource = generateUnauthenticatableResource();
      Set<DomainPermission> parentDomainPermissions_pre = setOf(domPerm_superuser_withGrant, domPerm_createchilddomain);
      accessControlContext.setDomainPermissions(accessorResource, parentDomain, parentDomainPermissions_pre);

      // set child domain permissions
      Set<DomainPermission> childDomainPermissions_pre = setOf(domPerm_createchilddomain);
      accessControlContext.setDomainPermissions(accessorResource, childDomain, childDomainPermissions_pre);

      // set donor permissions
      Resource donorResource = generateUnauthenticatableResource();
      Set<DomainPermission> parentDomainDonorPermissions_pre = setOf(domPerm_createchilddomain_withGrant);
      accessControlContext.setDomainPermissions(donorResource, childDomain, parentDomainDonorPermissions_pre);

      // set accessor --INHERIT-> donor
      Set<ResourcePermission> inheritanceResourcePermisions = setOf(ResourcePermissions.getInstance(ResourcePermissions.INHERIT));
      accessControlContext.setResourcePermissions(accessorResource, donorResource, inheritanceResourcePermisions);

      // verify
      if (!accessControlContext.hasDomainPermission(accessorResource, domPerm_superuser_withGrant, childDomain)) {
         fail("checking valid domain permissions inherited from ancestor domain and resource should have succeeded");
      }
      if (!accessControlContext.hasDomainPermission(accessorResource, domPerm_createchilddomain_withGrant, childDomain)) {
         fail("checking valid domain permissions inherited from ancestor domain and resource should have succeeded");
      }
   }

   @Test
   public void hasDomainPermission_whitespaceConsistent() {
      authenticateSystemResource();
      final DomainPermission domCreatePerm_superuser
            = DomainPermissions.getInstance(DomainPermissions.SUPER_USER);

      final String domainName = generateDomain();
      final String domainName_whitespaced = " " + domainName + "\t";

      // set domain create permissions
      Resource accessorResource = generateUnauthenticatableResource();
      Set<DomainPermission> domainPermissions_pre = new HashSet<>();
      domainPermissions_pre.add(domCreatePerm_superuser);
      accessControlContext.setDomainPermissions(accessorResource, domainName, domainPermissions_pre);

      // get domain create permissions and verify
      if (!accessControlContext.hasDomainPermission(accessorResource, domCreatePerm_superuser, domainName_whitespaced)) {
         fail("checking whitespaced domain permissions should have succeeded");
      }
   }

   @Test
   public void hasDomainPermission_nulls_shouldFail() {
      authenticateSystemResource();
      final Resource accessorResource = generateUnauthenticatableResource();
      final DomainPermission domPerm_superUser = DomainPermissions.getInstance(DomainPermissions.SUPER_USER);
      final String domainName = generateDomain();

      try {
         accessControlContext.hasDomainPermission(null, domPerm_superUser, domainName);
         fail("checking domain permissions with null accessor resource reference should have failed");
      }
      catch (NullPointerException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("resource required"));
      }

      try {
         accessControlContext.hasDomainPermission(accessorResource, null, domainName);
         fail("checking domain permissions with null domain permission reference should have failed");
      }
      catch (NullPointerException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("permission required"));
      }

      try {
         accessControlContext.hasDomainPermission(accessorResource, domPerm_superUser, null);
         fail("checking domain permissions with null domain name should have failed");
      }
      catch (NullPointerException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("domain required"));
      }
   }

   @Test
   public void hasDomainPermission_nonExistentReferences_shouldSucceed() {
      authenticateSystemResource();

      final DomainPermission domPerm_superUser = DomainPermissions.getInstance(DomainPermissions.SUPER_USER);
      final String domainName = generateDomain();
      final Resource invalidResource = Resources.getInstance(-999L);

      if (accessControlContext.hasDomainPermission(invalidResource, domPerm_superUser, domainName)) {
         // the check will "succeed" in the sense that it will fail to assert the permission on the
         // invalid resource, since that resource does not have the specified permission
         fail("checking domain permissions for invalid accessor resource should have failed");
      }
   }

   @Test
   public void hasDomainPermission_nonExistentReferences_shouldFail() {
      authenticateSystemResource();

      final Resource accessorResource = generateUnauthenticatableResource();
      final DomainPermission domPerm_superUser = DomainPermissions.getInstance(DomainPermissions.SUPER_USER);

      try {
         accessControlContext.hasDomainPermission(accessorResource, domPerm_superUser, "invalid_domain");
         fail("checking domain permissions with invalid domain name should have failed");
      }
      catch (IllegalArgumentException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("could not find domain"));
      }
   }
}
