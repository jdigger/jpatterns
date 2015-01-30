package com.mooregreatsoftware.jpatterns.builder;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * @see #withFirstName(String)
 */
@Immutable
public class Person {
    @Nonnull
    private final String firstName;
    @Nonnull
    private final String lastName;
    @Nonnull
    private final String title;


    @SuppressWarnings("ConstantConditions")
    private Person(@Nonnull String firstName, @Nonnull String lastName, @Nonnull String title) {
        if (firstName == null) throw new IllegalArgumentException("firstName == null");
        if (lastName == null) throw new IllegalArgumentException("lastName == null");
        if (title == null) throw new IllegalArgumentException("title == null");
        this.firstName = firstName;
        this.lastName = lastName;
        this.title = title;
    }


    @Nonnull
    public String getFirstName() {
        return firstName;
    }


    @Nonnull
    public String getLastName() {
        return lastName;
    }


    @Nonnull
    public String getTitle() {
        return title;
    }


    public static Builder withFirstName(@Nonnull String val) {
        return new Builder(val);
    }


    public static void main(String[] args) {
        Person cvc = Person.
            withFirstName("Jim").
            andLastName("Moore").
            andTitle("Code Master").
            build();

        assert cvc.getFirstName().equals("Jim");
        assert cvc.getLastName().equals("Moore");
        assert cvc.getTitle().equals("Code Master");
    }


    // **********************************************************************
    //
    // INNER CLASSES
    //
    // **********************************************************************

    /**
     * Class for creating an instance of {@link Person}
     *
     * @see Person#withFirstName(String)
     */
    public static class Builder {
        @Nonnull
        private final String firstName;


        private Builder(@Nonnull String val) {
            this.firstName = val;
        }


        public Builder2 andLastName(@Nonnull String val) {
            return new Builder2(val);
        }


        public class Builder2 {
            @Nonnull
            private final String lastName;


            private Builder2(@Nonnull String val) {
                this.lastName = val;
            }


            public Builder3 andTitle(@Nonnull String val) {
                return new Builder3(val);
            }


            public class Builder3 {
                @Nonnull
                private final String title;


                private Builder3(@Nonnull String val) {
                    this.title = val;
                }


                public Person build() {
                    return new Person(firstName, lastName, title);
                }
            }
        }
    }
}
