/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.rest.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;
import org.zanata.common.LocaleId;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
@XmlRootElement(name = "Entry")
@JsonPropertyOrder({ "Term" })
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonWriteNullProperties(false)
public class GlossaryEntry implements Serializable
{
   private List<GlossaryTerm> glossaryTerms;

   private LocaleId srcLang;

   @XmlElement(name = "Term")
   public List<GlossaryTerm> getGlossaryTerms()
   {
      if (glossaryTerms == null)
      {
         glossaryTerms = new ArrayList<GlossaryTerm>();
      }
      return glossaryTerms;
   }

   public void setGlossaryTerms(List<GlossaryTerm> glossaryTerms)
   {
      this.glossaryTerms = glossaryTerms;
   }

   @XmlAttribute(name = "srcLang")
   @XmlJavaTypeAdapter(type = LocaleId.class, value = LocaleIdAdapter.class)
   public LocaleId getSrcLang()
   {
      return srcLang;
   }

   public void setSrcLang(LocaleId srcLang)
   {
      this.srcLang = srcLang;
   }

   @Override
   public String toString()
   {
      return DTOUtil.toXML(this);
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((glossaryTerms == null) ? 0 : glossaryTerms.hashCode());
      result = prime * result + ((srcLang == null) ? 0 : srcLang.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (obj == null)
      {
         return false;
      }
      if (!(obj instanceof Glossary))
      {
         return false;
      }
      GlossaryEntry other = (GlossaryEntry) obj;
      if (glossaryTerms == null)
      {
         if (other.glossaryTerms != null)
         {
            return false;
         }
      }
      else if (!glossaryTerms.equals(other.glossaryTerms))
      {
         return false;
      }
      
      
      if (srcLang == null)
      {
         if (other.srcLang != null)
         {
            return false;
         }
      }
      else if (!srcLang.equals(other.srcLang))
      {
         return false;
      }
      
      return true;
   }
}


 