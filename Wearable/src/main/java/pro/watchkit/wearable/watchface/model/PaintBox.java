/*
 * Copyright (C) 2018-2019 Terence Tan
 *
 *  This file is free software: you may copy, redistribute and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or (at your
 *  option) any later version.
 *
 *  This file is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package pro.watchkit.wearable.watchface.model;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.Xfermode;
import android.os.Build;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.Objects;

import pro.watchkit.wearable.watchface.R;

public final class PaintBox {
    private static final float AMBIENT_PAINT_STROKE_WIDTH_PERCENT = 0.333f; // 0.333%
    private static final float PAINT_STROKE_WIDTH_PERCENT = 0.5f; // 0.5%
    private static String[] wikipediaNames = {
            "Absolute Zero",
            "Acajou",
            "Acid Green",
            "Aero",
            "Aero Blue",
            "African Violet",
            "Air Force Blue (RAF)",
            "Air Force Blue (USAF)",
            "Air Superiority Blue",
            "Alabama Crimson",
            "Alabaster",
            "Alice Blue",
            "Alien Armpit",
            "Alizarin Crimson",
            "Alloy Orange",
            "Almond",
            "Amaranth",
            "Amaranth Deep Purple",
            "Amaranth Pink",
            "Amaranth Purple",
            "Amaranth Red",
            "Amazon",
            "Amazonite",
            "Amber",
            "Amber (SAE/ECE)",
            "American Blue",
            "American Brown",
            "American Gold",
            "American Green",
            "American Orange",
            "American Pink",
            "American Purple",
            "American Red",
            "American Rose",
            "American Silver",
            "American Violet",
            "American Yellow",
            "Amethyst",
            "Android Green",
            "Anti-Flash White",
            "Antique Brass",
            "Antique Bronze",
            "Antique Fuchsia",
            "Antique Ruby",
            "Antique White",
            "Ao (English)",
            "Apple",
            "Apple Green",
            "Apricot",
            "Aqua",
            "Aquamarine",
            "Arctic Lime",
            "Army Green",
            "Arsenic",
            "Artichoke",
            "Arylide Yellow",
            "Ash Gray",
            "Asparagus",
            "Ateneo Blue",
            "Atomic Tangerine",
            "Auburn",
            "Aureolin",
            "AuroMetalSaurus",
            "Avocado",
            "Awesome",
            "Axolotl",
            "Aztec Gold",
            "Azure",
            "Azure (Web Color)",
            "Azure Mist",
            "Azureish White",
            "Baby Blue",
            "Baby Blue Eyes",
            "Baby Pink",
            "Baby Powder",
            "Baker-Miller Pink",
            "Ball Blue",
            "Banana Mania",
            "Banana Yellow",
            "Bangladesh Green",
            "Barbie Pink",
            "Barn Red",
            "Battery Charged Blue",
            "Battleship Grey",
            "Bazaar",
            "Beau Blue",
            "Beaver",
            "Begonia",
            "Beige",
            "B'dazzled Blue",
            "Big Dip O’ruby",
            "Big Foot Feet",
            "Bisque",
            "Bistre",
            "Bistre Brown",
            "Bitter Lemon",
            "Bitter Lime",
            "Bittersweet",
            "Bittersweet Shimmer",
            "Black",
            "Black Bean",
            "Black Chocolate",
            "Black Coffee",
            "Black Coral",
            "Black Leather Jacket",
            "Black Olive",
            "Blackberry",
            "Black Shadows",
            "Blanched Almond",
            "Blast-Off Bronze",
            "Bleu De France",
            "Blizzard Blue",
            "Blond",
            "Blood Orange",
            "Blood Red",
            "Blue",
            "Blue (Crayola)",
            "Blue (Munsell)",
            "Blue (NCS)",
            "Blue (Pantone)",
            "Blue (Pigment)",
            "Blue (RYB)",
            "Blue Bell",
            "Blue Bolt",
            "Blue-Gray",
            "Blue-Green",
            "Blue Jeans",
            "Blue Lagoon",
            "Blue-Magenta Violet",
            "Blue Sapphire",
            "Blue-Violet",
            "Blue Yonder",
            "Blueberry",
            "Bluebonnet",
            "Blush",
            "Bole",
            "Bondi Blue",
            "Bone",
            "Booger Buster",
            "Boston University Red",
            "Bottle Green",
            "Boysenberry",
            "Brandeis Blue",
            "Brass",
            "Brick Red",
            "Bright Cerulean",
            "Bright Gray",
            "Bright Green",
            "Bright Lavender",
            "Bright Lilac",
            "Bright Maroon",
            "Bright Navy Blue",
            "Bright Pink",
            "Bright Turquoise",
            "Bright Ube",
            "Bright Yellow (Crayola)",
            "Brilliant Azure",
            "Brilliant Lavender",
            "Brilliant Rose",
            "Brink Pink",
            "British Racing Green",
            "Bronze",
            "Bronze",
            "Bronze (Metallic)",
            "Bronze Yellow",
            "Brown",
            "Brown (Crayola)",
            "Brown (Traditional)",
            "Brown (Web)",
            "Brown-Nose",
            "Brown Sugar",
            "Brown Chocolate",
            "Brown Coffee",
            "Brown Yellow",
            "Brunswick Green",
            "Bubble Gum",
            "Bubbles",
            "Bud Green",
            "Buff",
            "Bulgarian Rose",
            "Burgundy",
            "Burlywood",
            "Burnished Brown",
            "Burnt Orange",
            "Burnt Sienna",
            "Burnt Umber",
            "Button Blue",
            "Byzantine",
            "Byzantium",
            "Cadet",
            "Cadet Blue",
            "Cadet Grey",
            "Cadmium Blue",
            "Cadmium Green",
            "Cadmium Orange",
            "Cadmium Purple",
            "Cadmium Red",
            "Cadmium Yellow",
            "Cadmium Violet",
            "Café Au Lait",
            "Café Noir",
            "Cal Poly Pomona Green",
            "Calamansi",
            "Cambridge Blue",
            "Camel",
            "Cameo Pink",
            "Camouflage Green",
            "Canary",
            "Canary Yellow",
            "Candy Apple Red",
            "Candy Pink",
            "Capri",
            "Caput Mortuum",
            "Caramel",
            "Cardinal",
            "Caribbean Green",
            "Carmine",
            "Carmine (M&P)",
            "Carmine Pink",
            "Carmine Red",
            "Carnation Pink",
            "Carnelian",
            "Carolina Blue",
            "Carrot Orange",
            "Castleton Green",
            "Catalina Blue",
            "Catawba",
            "Cedar Chest",
            "Ceil",
            "Celadon",
            "Celadon Blue",
            "Celadon Green",
            "Celeste",
            "Celestial Blue",
            "Cerise",
            "Cerise Pink",
            "Cerulean",
            "Cerulean Blue",
            "Cerulean Frost",
            "CG Blue",
            "CG Red",
            "Chamoisee",
            "Champagne",
            "Champagne Pink",
            "Charcoal",
            "Charleston Green",
            "Charm",
            "Charm Pink",
            "Chartreuse (Traditional)",
            "Chartreuse (Web)",
            "Cheese",
            "Cherry",
            "Cherry Blossom Pink",
            "Chestnut",
            "China Pink",
            "China Rose",
            "Chinese Black",
            "Chinese Blue",
            "Chinese Bronze",
            "Chinese Brown",
            "Chinese Green",
            "Chinese Gold",
            "Chinese Orange",
            "Chinese Pink",
            "Chinese Purple",
            "Chinese Red",
            "Chinese Red",
            "Chinese Silver",
            "Chinese Violet",
            "Chinese White",
            "Chinese Yellow",
            "Chlorophyll Green",
            "Chocolate Kisses",
            "Chocolate (Traditional)",
            "Chocolate (Web)",
            "Christmas Blue",
            "Christmas Blue",
            "Christmas Brown",
            "Christmas Brown",
            "Christmas Green",
            "Christmas Green",
            "Christmas Gold",
            "Christmas Orange",
            "Christmas Orange",
            "Christmas Pink",
            "Christmas Pink",
            "Christmas Purple",
            "Christmas Purple",
            "Christmas Red",
            "Christmas Red",
            "Christmas Silver",
            "Christmas Yellow",
            "Christmas Yellow",
            "Chrome Yellow",
            "Cinereous",
            "Cinnabar",
            "Cinnamon[Citation Needed]",
            "Cinnamon Satin",
            "Citrine",
            "Citrine Brown",
            "Citron",
            "Claret",
            "Classic Rose",
            "Cobalt Blue",
            "Cocoa Brown",
            "Coconut",
            "Coffee",
            "Cola",
            "Columbia Blue",
            "Conditioner",
            "Congo Pink",
            "Cool Black",
            "Cool Grey",
            "Cookies And Cream",
            "Copper",
            "Copper (Crayola)",
            "Copper Penny",
            "Copper Red",
            "Copper Rose",
            "Coquelicot",
            "Coral",
            "Coral Pink",
            "Coral Red",
            "Coral Reef",
            "Cordovan",
            "Corn",
            "Cornell Red",
            "Cornflower Blue",
            "Cornsilk",
            "Cosmic Cobalt",
            "Cosmic Latte",
            "Coyote Brown",
            "Cotton Candy",
            "Cream",
            "Crimson",
            "Crimson Glory",
            "Crimson Red",
            "Cultured",
            "Cyan",
            "Cyan Azure",
            "Cyan-Blue Azure",
            "Cyan Cobalt Blue",
            "Cyan Cornflower Blue",
            "Cyan (Process)",
            "Cyber Grape",
            "Cyber Yellow",
            "Cyclamen",
            "Daffodil",
            "Dandelion",
            "Dark Blue",
            "Dark Blue-Gray",
            "Dark Bronze",
            "Dark Brown",
            "Dark Brown-Tangelo",
            "Dark Byzantium",
            "Dark Candy Apple Red",
            "Dark Cerulean",
            "Dark Charcoal",
            "Dark Chestnut",
            "Dark Chocolate",
            "Dark Chocolate (Hershey's)",
            "Dark Cornflower Blue",
            "Dark Coral",
            "Dark Cyan",
            "Dark Electric Blue",
            "Dark Goldenrod",
            "Dark Gray (X11)",
            "Dark Green",
            "Dark Green (X11)",
            "Dark Gunmetal",
            "Dark Imperial Blue",
            "Dark Imperial Blue",
            "Dark Jungle Green",
            "Dark Khaki",
            "Dark Lava",
            "Dark Lavender",
            "Dark Lemon Lime",
            "Dark Liver",
            "Dark Liver (Horses)",
            "Dark Magenta",
            "Dark Medium Gray",
            "Dark Midnight Blue",
            "Dark Moss Green",
            "Dark Olive Green",
            "Dark Orange",
            "Dark Orchid",
            "Dark Pastel Blue",
            "Dark Pastel Green",
            "Dark Pastel Purple",
            "Dark Pastel Red",
            "Dark Pink",
            "Dark Powder Blue",
            "Dark Puce",
            "Dark Purple",
            "Dark Raspberry",
            "Dark Red",
            "Dark Salmon",
            "Dark Scarlet",
            "Dark Sea Green",
            "Dark Sienna",
            "Dark Sky Blue",
            "Dark Slate Blue",
            "Dark Slate Gray",
            "Dark Spring Green",
            "Dark Tan",
            "Dark Tangerine",
            "Dark Taupe",
            "Dark Terra Cotta",
            "Dark Turquoise",
            "Dark Vanilla",
            "Dark Violet",
            "Dark Yellow",
            "Dartmouth Green",
            "Davy's Grey",
            "Debian Red",
            "Deep Amethyst",
            "Deep Aquamarine",
            "Deep Carmine",
            "Deep Carmine Pink",
            "Deep Carrot Orange",
            "Deep Cerise",
            "Deep Champagne",
            "Deep Chestnut",
            "Deep Coffee",
            "Deep Fuchsia",
            "Deep Green",
            "Deep Green-Cyan Turquoise",
            "Deep Jungle Green",
            "Deep Koamaru",
            "Deep Lemon",
            "Deep Lilac",
            "Deep Magenta",
            "Deep Maroon",
            "Deep Mauve",
            "Deep Moss Green",
            "Deep Peach",
            "Deep Pink",
            "Deep Puce",
            "Deep Red",
            "Deep Ruby",
            "Deep Saffron",
            "Deep Sky Blue",
            "Deep Space Sparkle",
            "Deep Spring Bud",
            "Deep Taupe",
            "Deep Tuscan Red",
            "Deep Violet",
            "Deer",
            "Denim",
            "Denim Blue",
            "Desaturated Cyan",
            "Desert",
            "Desert Sand",
            "Desire",
            "Diamond",
            "Dim Gray",
            "Dingy Dungeon",
            "Dirt",
            "Dirty Brown",
            "Dirty White",
            "Dodger Blue",
            "Dodie Yellow",
            "Dogwood Rose",
            "Dollar Bill",
            "Dolphin Gray",
            "Donkey Brown",
            "Drab",
            "Duke Blue",
            "Dust Storm",
            "Dutch White",
            "Earth Yellow",
            "Ebony",
            "Ecru",
            "Eerie Black",
            "Eggplant",
            "Eggshell",
            "Egyptian Blue",
            "Electric Blue",
            "Electric Crimson",
            "Electric Cyan",
            "Electric Green",
            "Electric Indigo",
            "Electric Lavender",
            "Electric Lime",
            "Electric Purple",
            "Electric Ultramarine",
            "Electric Violet",
            "Electric Yellow",
            "Emerald",
            "Emerald Green",
            "Eminence",
            "English Green",
            "English Lavender",
            "English Red",
            "English Vermillion",
            "English Violet",
            "Eton Blue",
            "Eucalyptus",
            "Fallow",
            "Falu Red",
            "Fandango",
            "Fandango Pink",
            "Fashion Fuchsia",
            "Fawn",
            "Feldgrau",
            "Feldspar",
            "Fern Green",
            "Ferrari Red",
            "Field Drab",
            "Fiery Rose",
            "Firebrick",
            "Fire Engine Red",
            "Fire Opal",
            "Flame",
            "Flamingo Pink",
            "Flattery",
            "Flavescent",
            "Flax",
            "Flesh",
            "Flirt",
            "Floral White",
            "Fluorescent Orange",
            "Fluorescent Pink",
            "Fluorescent Yellow",
            "Folly",
            "Forest Green (Traditional)",
            "Forest Green (Web)",
            "French Beige",
            "French Bistre",
            "French Blue",
            "French Fuchsia",
            "French Lilac",
            "French Lime",
            "French Mauve",
            "French Pink",
            "French Plum",
            "French Puce",
            "French Raspberry",
            "French Rose",
            "French Sky Blue",
            "French Violet",
            "French Wine",
            "Fresh Air",
            "Frostbite",
            "Fuchsia",
            "Fuchsia (Crayola)",
            "Fuchsia Pink",
            "Fuchsia Purple",
            "Fuchsia Rose",
            "Fulvous",
            "Fuzzy Wuzzy",
            "Gainsboro",
            "Gamboge",
            "Gamboge Orange (Brown)",
            "Granet",
            "Gargoyle Gas",
            "Generic Viridian",
            "Ghost White",
            "Giant's Club",
            "Giants Orange",
            "Ginger",
            "Glaucous",
            "Glitter",
            "Glossy Grape",
            "GO Green",
            "Gold",
            "Gold (Metallic)",
            "Gold (Web) (Golden)",
            "Gold (Crayola)",
            "Gold Fusion",
            "Gold Foil",
            "Golden Brown",
            "Golden Poppy",
            "Golden Yellow",
            "Goldenrod",
            "Granite Gray",
            "Granny Smith Apple",
            "Grape",
            "Gray (HTML/CSS Gray)",
            "Gray (X11 Gray)",
            "Gray-Asparagus",
            "Gray-Blue",
            "Green",
            "Green (Color Wheel) (X11 Green)",
            "Green (Crayola)",
            "Green (HTML/CSS Color)",
            "Green (Munsell)",
            "Green (NCS)",
            "Green (Pantone)",
            "Green (Pigment)",
            "Green (RYB)",
            "Green-Blue",
            "Green-Cyan",
            "Green Lizard",
            "Green Sheen",
            "Green-Yellow",
            "Grizzly",
            "Grullo",
            "Guppie Green",
            "Gunmetal",
            "Halayà Úbe",
            "Halloween Orange",
            "Han Blue",
            "Han Purple",
            "Hansa Yellow",
            "Harlequin",
            "Harlequin Green",
            "Harvard Crimson",
            "Harvest Gold",
            "Heart Gold",
            "Heat Wave",
            "Heidelberg Red[2]",
            "Heliotrope",
            "Heliotrope Gray",
            "Heliotrope Magenta",
            "Hollywood Cerise",
            "Honeydew",
            "Honolulu Blue",
            "Hooker's Green",
            "Hot Magenta",
            "Hot Pink",
            "Hunter Green",
            "Iceberg",
            "Icterine",
            "Iguana Green",
            "Illuminating Emerald",
            "Imperial",
            "Imperial Blue",
            "Imperial Purple",
            "Imperial Red",
            "Inchworm",
            "Independence",
            "India Green",
            "Indian Red",
            "Indian Yellow",
            "Indigo",
            "Indigo Dye",
            "Indigo (Rainbow)",
            "Indigo (Web)",
            "Infra Red",
            "Interdimensional Blue",
            "International Klein Blue",
            "International Orange (Aerospace)",
            "International Orange (Engineering)",
            "International Orange (Golden Gate Bridge)",
            "Iris",
            "Irresistible",
            "Isabelline",
            "Italian Sky Blue",
            "Ivory",
            "Jacarta",
            "Jacko Bean",
            "Jade",
            "Japanese Carmine",
            "Japanese Indigo",
            "Japanese Laurel",
            "Japanese Violet",
            "Jasmine",
            "Jasper",
            "Jasper Orange",
            "Jazzberry Jam",
            "Jelly Bean",
            "Jelly Bean Blue",
            "Jet",
            "Jet Stream",
            "Jonquil",
            "Jordy Blue",
            "June Bud",
            "Jungle Green",
            "Kelly Green",
            "Kenyan Copper",
            "Keppel",
            "Key Lime",
            "Khaki (HTML/CSS) (Khaki)",
            "Khaki (X11) (Light Khaki)",
            "Kiwi",
            "Kobe",
            "Kobi",
            "Kobicha",
            "Kombu Green",
            "KSU Purple",
            "KU Crimson",
            "La Salle Green",
            "Languid Lavender",
            "Lapis Lazuli",
            "Laser Lemon",
            "Laurel Green",
            "Lava",
            "Lavender (Floral)",
            "Lavender (Web)",
            "Lavender Blue",
            "Lavender Blush",
            "Lavender Gray",
            "Lavender Indigo",
            "Lavender Magenta",
            "Lavender Mist",
            "Lavender Pink",
            "Lavender Purple",
            "Lavender Rose",
            "Lawn Green",
            "Lemon",
            "Lemon Chiffon",
            "Lemon Curry",
            "Lemon Glacier",
            "Lemon Lime",
            "Lemon Meringue",
            "Lemon Yellow",
            "Lemon Yellow (Crayola)",
            "Lenurple",
            "Liberty",
            "Licorice",
            "Light Apricot",
            "Light Blue",
            "Light Brilliant Red",
            "Light Brown",
            "Light Carmine Pink",
            "Light Cobalt Blue",
            "Light Coral",
            "Light Cornflower Blue",
            "Light Crimson",
            "Light Cyan",
            "Light Deep Pink",
            "Light French Beige",
            "Light Fuchsia Pink",
            "Light Gold",
            "Light Goldenrod Yellow",
            "Light Gray",
            "Light Grayish Magenta",
            "Light Green",
            "Light Hot Pink",
            "Light Khaki",
            "Light Medium Orchid",
            "Light Moss Green",
            "Light Orange",
            "Light Orchid",
            "Light Pastel Purple",
            "Light Periwinkle",
            "Light Pink",
            "Light Red",
            "Light Red Ochre",
            "Light Salmon",
            "Light Salmon Pink",
            "Light Sea Green",
            "Light Silver",
            "Light Sky Blue",
            "Light Slate Gray",
            "Light Steel Blue",
            "Light Taupe",
            "Light Thulian Pink",
            "Light Yellow",
            "Lilac",
            "Lilac Luster",
            "Lime (Color Wheel)",
            "Lime (Web) (X11 Green)",
            "Lime Green",
            "Limerick",
            "Lincoln Green",
            "Linen",
            "Lion",
            "Liseran Purple",
            "Little Boy Blue",
            "Little Girl Pink",
            "Liver",
            "Liver (Dogs)",
            "Liver (Organ)",
            "Liver Chestnut",
            "Livid",
            "Loeen Look",
            "Lotion",
            "Lumber",
            "Lust",
            "Maastricht Blue",
            "Macaroni And Cheese",
            "Madder Lake",
            "Magenta",
            "Magenta (Crayola)",
            "Magenta (Dye)",
            "Magenta (Pantone)",
            "Magenta (Process)",
            "Magenta Haze",
            "Magenta-Pink",
            "Magic Mint",
            "Magic Potion",
            "Magnolia",
            "Mahogany",
            "Maize",
            "Maize (Crayola)",
            "Majorelle Blue",
            "Malachite",
            "Manatee",
            "Mandarin",
            "Mango Green",
            "Mango Tango",
            "Mango Yellow",
            "Mantis",
            "Mardi Gras",
            "Marigold",
            "Maroon (Crayola)",
            "Maroon (HTML/CSS)",
            "Maroon (X11)",
            "Mauve",
            "Mauve Taupe",
            "Mauvelous",
            "Maximum Blue",
            "Maximum Blue Green",
            "Maximum Blue Purple",
            "Maximum Green",
            "Maximum Green Yellow",
            "Maximum Purple",
            "Maximum Red",
            "Maximum Red Purple",
            "Maximum Yellow",
            "Maximum Yellow Red",
            "May Green",
            "Maya Blue",
            "Meat Brown",
            "Medium Aquamarine",
            "Medium Blue",
            "Medium Candy Apple Red",
            "Medium Carmine",
            "Medium Champagne",
            "Medium Electric Blue",
            "Medium Jungle Green",
            "Medium Lavender Magenta",
            "Medium Orchid",
            "Medium Persian Blue",
            "Medium Purple",
            "Medium Red-Violet",
            "Medium Ruby",
            "Medium Sea Green",
            "Medium Sky Blue",
            "Medium Slate Blue",
            "Medium Spring Bud",
            "Medium Spring Green",
            "Medium Taupe",
            "Medium Turquoise",
            "Medium Tuscan Red",
            "Medium Vermilion",
            "Medium Violet-Red",
            "Mellow Apricot",
            "Mellow Yellow",
            "Melon",
            "Menthol",
            "Metallic Blue",
            "Metallic Bronze",
            "Metallic Brown",
            "Metallic Gold",
            "Metallic Green",
            "Metallic Orange",
            "Metallic Pink",
            "Metallic Red",
            "Metallic Seaweed",
            "Metallic Silver",
            "Metallic Sunburst",
            "Metallic Violet",
            "Metallic Yellow",
            "Mexican Pink",
            "Middle Blue",
            "Middle Blue Green",
            "Middle Blue Purple",
            "Middle Grey",
            "Middle Green",
            "Middle Green Yellow",
            "Middle Purple",
            "Middle Red",
            "Middle Red Purple",
            "Middle Yellow",
            "Middle Yellow Red",
            "Midnight",
            "Midnight Blue",
            "Midnight Blue",
            "Midnight Green (Eagle Green)",
            "Mikado Yellow",
            "Milk",
            "Milk Chocolate",
            "Mimi Pink",
            "Mindaro",
            "Ming",
            "Minion Yellow",
            "Mint",
            "Mint Cream",
            "Mint Green",
            "Misty Moss",
            "Misty Rose",
            "Moccasin",
            "Mode Beige",
            "Moonstone",
            "Moonstone Blue",
            "Mordant Red 19",
            "Morning Blue",
            "Moss Green",
            "Mountain Meadow",
            "Mountbatten Pink",
            "MSU Green",
            "Mud",
            "Mughal Green",
            "Mulberry",
            "Mulberry (Crayola)",
            "Mummy's Tomb",
            "Mustard",
            "Mustard Brown",
            "Mustard Green",
            "Mustard Yellow",
            "Myrtle Green",
            "Mystic",
            "Mystic Maroon",
            "Mystic Red",
            "Nadeshiko Pink",
            "Napier Green",
            "Naples Yellow",
            "Navajo White",
            "Navy",
            "Navy Blue",
            "Navy Blue (Crayola)",
            "Navy Purple",
            "Neon Blue",
            "Neon Brown",
            "Neon Carrot",
            "Neon Cyan",
            "Neon Fuchsia",
            "Neon Gold",
            "Neon Gray",
            "Neon Green",
            "Neon Orange",
            "Neon Pink",
            "Neon Purple",
            "Neon Red",
            "Neon Scarlet",
            "Neon Silver",
            "Neon Tangerine",
            "Neon Yellow",
            "New Car",
            "New York Pink",
            "Nickel",
            "Non-Photo Blue",
            "North Texas Green",
            "Nyanza",
            "Ocean Blue",
            "Ocean Boat Blue",
            "Ocean Green",
            "Ochre",
            "Office Green",
            "Ogre Odor",
            "Old Burgundy",
            "Old Gold",
            "Old Heliotrope",
            "Old Lace",
            "Old Lavender",
            "Old Mauve",
            "Old Moss Green",
            "Old Rose",
            "Old Silver",
            "Olive",
            "Olive Drab 3",
            "Olive Drab 7",
            "Olivine",
            "Onyx",
            "Opal",
            "Opera Mauve",
            "Orange (Color Wheel)",
            "Orange (Crayola)",
            "Orange (Pantone)",
            "Orange (RYB)",
            "Orange (Web)",
            "Orange Peel",
            "Orange-Red",
            "Orange Soda",
            "Orange-Yellow",
            "Orchid",
            "Orchid Pink",
            "Orioles Orange",
            "Otter Brown",
            "Outer Space",
            "Outrageous Orange",
            "Oxford Blue",
            "Oxley",
            "OU Crimson Red",
            "Pacific Blue",
            "Pakistan Green",
            "Palatinate Blue",
            "Palatinate Purple",
            "Pale Aqua",
            "Pale Blue",
            "Pale Brown",
            "Pale Carmine",
            "Pale Cerulean",
            "Pale Chestnut",
            "Pale Copper",
            "Pale Cornflower Blue",
            "Pale Cyan",
            "Pale Gold",
            "Pale Goldenrod",
            "Pale Green",
            "Pale Lavender",
            "Pale Magenta",
            "Pale Magenta-Pink",
            "Pale Pink",
            "Pale Plum",
            "Pale Red-Violet",
            "Pale Robin Egg Blue",
            "Pale Silver",
            "Pale Spring Bud",
            "Pale Taupe",
            "Pale Turquoise",
            "Pale Violet",
            "Pale Violet-Red",
            "Palm Leaf",
            "Pansy Purple",
            "Paolo Veronese Green",
            "Papaya Whip",
            "Paradise Pink",
            "Paris Green",
            "Parrot Pink",
            "Pastel Blue",
            "Pastel Brown",
            "Pastel Gray",
            "Pastel Green",
            "Pastel Magenta",
            "Pastel Orange",
            "Pastel Pink",
            "Pastel Purple",
            "Pastel Red",
            "Pastel Violet",
            "Pastel Yellow",
            "Patriarch",
            "Payne's Grey",
            "Peach",
            "Peach",
            "Peach-Orange",
            "Peach Puff",
            "Peach-Yellow",
            "Pear",
            "Pearl",
            "Pearl Aqua",
            "Pearly Purple",
            "Peridot",
            "Periwinkle",
            "Periwinkle (Crayola)",
            "Permanent Geranium Lake",
            "Persian Blue",
            "Persian Green",
            "Persian Indigo",
            "Persian Orange",
            "Persian Pink",
            "Persian Plum",
            "Persian Red",
            "Persian Rose",
            "Persimmon",
            "Peru",
            "Pewter Blue",
            "Philippine Blue",
            "Philippine Brown",
            "Philippine Gold",
            "Philippine Golden Yellow",
            "Philippine Gray",
            "Philippine Green",
            "Philippine Orange",
            "Philippine Pink",
            "Philippine Red",
            "Philippine Silver",
            "Philippine Violet",
            "Philippine Yellow",
            "Phlox",
            "Phthalo Blue",
            "Phthalo Green",
            "Picton Blue",
            "Pictorial Carmine",
            "Piggy Pink",
            "Pine Green",
            "Pine Tree",
            "Pineapple",
            "Pink",
            "Pink (Pantone)",
            "Pink Flamingo",
            "Pink Lace",
            "Pink Lavender",
            "Pink-Orange",
            "Pink Pearl",
            "Pink Raspberry",
            "Pink Sherbet",
            "Pistachio",
            "Pixie Powder",
            "Platinum",
            "Plum",
            "Plum (Web)",
            "Plump Purple",
            "Police Blue",
            "Polished Pine",
            "Pomp And Power",
            "Popstar",
            "Portland Orange",
            "Powder Blue",
            "Princess Perfume",
            "Princeton Orange",
            "Prune",
            "Prussian Blue",
            "Psychedelic Purple",
            "Puce",
            "Puce Red",
            "Pullman Brown (UPS Brown)",
            "Pullman Green",
            "Pumpkin",
            "Purple (HTML)",
            "Purple (Munsell)",
            "Purple (X11)",
            "Purple Heart",
            "Purple Mountain Majesty",
            "Purple Navy",
            "Purple Pizzazz",
            "Purple Plum",
            "Purple Taupe",
            "Purpureus",
            "Quartz",
            "Queen Blue",
            "Queen Pink",
            "Quick Silver",
            "Quinacridone Magenta",
            "Quincy",
            "Rackley",
            "Radical Red",
            "Raisin Black",
            "Rajah",
            "Raspberry",
            "Raspberry Glace",
            "Raspberry Pink",
            "Raspberry Rose",
            "Raw Sienna",
            "Raw Umber",
            "Razzle Dazzle Rose",
            "Razzmatazz",
            "Razzmic Berry",
            "Rebecca Purple",
            "Red",
            "Red (Crayola)",
            "Red (Munsell)",
            "Red (NCS)",
            "Red (Pantone)",
            "Red (Pigment)",
            "Red (RYB)",
            "Red-Brown",
            "Red Devil",
            "Red-Orange",
            "Red-Purple",
            "Red Salsa",
            "Red-Violet",
            "Redwood",
            "Regalia",
            "Registration Black",
            "Resolution Blue",
            "Rhythm",
            "Rich Black",
            "Rich Black (FOGRA29)",
            "Rich Black (FOGRA39)",
            "Rich Brilliant Lavender",
            "Rich Carmine",
            "Rich Electric Blue",
            "Rich Lavender",
            "Rich Lilac",
            "Rich Maroon",
            "Rifle Green",
            "Roast Coffee",
            "Robin Egg Blue",
            "Rocket Metallic",
            "Roman Silver",
            "Root Beer",
            "Rose",
            "Rose Bonbon",
            "Rose Dust",
            "Rose Ebony",
            "Rose Garnet",
            "Rose Gold",
            "Rose Madder",
            "Rose Pink",
            "Rose Quartz",
            "Rose Quartz Pink",
            "Rose Red",
            "Rose Taupe",
            "Rose Vale",
            "Rosewood",
            "Rosso Corsa",
            "Rosy Brown",
            "Royal Azure",
            "Royal Blue",
            "Royal Blue",
            "Royal Brown",
            "Royal Fuchsia",
            "Royal Green",
            "Royal Orange",
            "Royal Pink",
            "Royal Red",
            "Royal Red",
            "Royal Purple",
            "Royal Yellow",
            "Ruber",
            "Rubine Red",
            "Ruby",
            "Ruby Red",
            "Ruddy",
            "Ruddy Brown",
            "Ruddy Pink",
            "Rufous",
            "Russet",
            "Russian Green",
            "Russian Violet",
            "Rust",
            "Rusty Red",
            "Sacramento State Green",
            "Saddle Brown",
            "Safety Orange",
            "Safety Orange (Blaze Orange)",
            "Safety Yellow",
            "Saffron",
            "Sage",
            "St. Patrick's Blue",
            "Salem (Color)",
            "Salmon",
            "Salmon Pink",
            "Sand",
            "Sand Dune",
            "Sandstorm",
            "Sandy Brown",
            "Sandy Tan",
            "Sandy Taupe",
            "Sangria",
            "Sap Green",
            "Sapphire",
            "Sapphire Blue",
            "Sasquatch Socks",
            "Satin Sheen Gold",
            "Scarlet",
            "Scarlet",
            "Schauss Pink",
            "School Bus Yellow",
            "Screamin' Green",
            "Sea Blue",
            "Sea Foam Green",
            "Sea Green",
            "Sea Serpent",
            "Seal Brown",
            "Seashell",
            "Selective Yellow",
            "Sepia",
            "Shadow",
            "Shadow Blue",
            "Shampoo",
            "Shamrock Green",
            "Sheen Green",
            "Shimmering Blush",
            "Shiny Shamrock",
            "Shocking Pink",
            "Shocking Pink (Crayola)",
            "Sienna",
            "Silver",
            "Silver (Crayola)",
            "Silver (Metallic)",
            "Silver Chalice",
            "Silver Foil",
            "Silver Lake Blue",
            "Silver Pink",
            "Silver Sand",
            "Sinopia",
            "Sizzling Red",
            "Sizzling Sunrise",
            "Skobeloff",
            "Sky Blue",
            "Sky Blue (Crayola)",
            "Sky Magenta",
            "Slate Blue",
            "Slate Gray",
            "Slimy Green",
            "Smalt (Dark Powder Blue)",
            "Smashed Pumpkin",
            "Smitten",
            "Smoke",
            "Smokey Topaz",
            "Smoky Black",
            "Smoky Topaz",
            "Snow",
            "Soap",
            "Soldier Green",
            "Solid Pink",
            "Sonic Silver",
            "Spartan Crimson",
            "Space Cadet",
            "Spanish Bistre",
            "Spanish Blue",
            "Spanish Carmine",
            "Spanish Crimson",
            "Spanish Gray",
            "Spanish Green",
            "Spanish Orange",
            "Spanish Pink",
            "Spanish Purple",
            "Spanish Red",
            "Spanish Sky Blue",
            "Spanish Violet",
            "Spanish Viridian",
            "Spanish Yellow",
            "Spicy Mix",
            "Spiro Disco Ball",
            "Spring Bud",
            "Spring Frost",
            "Spring Green",
            "Spring Green (Crayola)",
            "Star Command Blue",
            "Steel Blue",
            "Steel Pink",
            "Steel Teal",
            "Stil De Grain Yellow",
            "Stizza",
            "Stormcloud",
            "Straw",
            "Strawberry",
            "Sugar Plum",
            "Sunburnt Cyclops",
            "Sunglow",
            "Sunny",
            "Sunray",
            "Sunset",
            "Sunset Orange",
            "Super Pink",
            "Sweet Brown",
            "Tan",
            "Tangelo",
            "Tangerine",
            "Tangerine Yellow",
            "Tango Pink",
            "Tart Orange",
            "Taupe",
            "Taupe Gray",
            "Tea Green",
            "Tea Rose",
            "Tea Rose",
            "Teal",
            "Teal Blue",
            "Teal Deer",
            "Teal Green",
            "Telemagenta",
            "Temptress",
            "Tenné (Tawny)",
            "Terra Cotta",
            "Thistle",
            "Thulian Pink",
            "Tickle Me Pink",
            "Tiffany Blue",
            "Tiger's Eye",
            "Timberwolf",
            "Titanium",
            "Titanium Yellow",
            "Tomato",
            "Toolbox",
            "Topaz",
            "Tractor Red",
            "Trolley Grey",
            "Tropical Rain Forest",
            "Tropical Violet",
            "True Blue",
            "Tufts Blue",
            "Tulip",
            "Tumbleweed",
            "Turkish Rose",
            "Turquoise",
            "Turquoise Blue",
            "Turquoise Green",
            "Turquoise Surf",
            "Turtle Green",
            "Tuscan",
            "Tuscan Brown",
            "Tuscan Red",
            "Tuscan Tan",
            "Tuscany",
            "Twilight Lavender",
            "Tyrian Purple",
            "UA Blue",
            "UA Red",
            "Ube",
            "UCLA Blue",
            "UCLA Gold",
            "UE Red",
            "UFO Green",
            "Ultramarine",
            "Ultramarine Blue",
            "Ultra Pink",
            "Ultra Red",
            "Umber",
            "Unbleached Silk",
            "United Nations Blue",
            "University Of California Gold",
            "University Of Tennessee Orange",
            "Unmellow Yellow",
            "UP Forest Green",
            "UP Maroon",
            "Upsdell Red",
            "Urobilin",
            "USAFA Blue",
            "USC Cardinal",
            "USC Gold",
            "Utah Crimson",
            "Vampire Black",
            "Van Dyke Brown",
            "Vanilla",
            "Vanilla Ice",
            "Vegas Gold",
            "Venetian Red",
            "Verdigris",
            "Vermilion",
            "Vermilion",
            "Veronica",
            "Verse Green",
            "Very Light Azure",
            "Very Light Blue",
            "Very Light Malachite Green",
            "Very Light Tangelo",
            "Very Pale Orange",
            "Very Pale Yellow",
            "Violet",
            "Violet-Blue",
            "Violet-Red",
            "Violin Brown",
            "Viridian",
            "Viridian Green",
            "Vista Blue",
            "Vivid Amber",
            "Vivid Auburn",
            "Vivid Burgundy",
            "Vivid Cerise",
            "Vivid Cerulean",
            "Vivid Crimson",
            "Vivid Gamboge",
            "Vivid Lime Green",
            "Vivid Malachite",
            "Vivid Mulberry",
            "Vivid Orange",
            "Vivid Orange Peel",
            "Vivid Orchid",
            "Vivid Raspberry",
            "Vivid Red",
            "Vivid Red-Tangelo",
            "Vivid Sky Blue",
            "Vivid Tangelo",
            "Vivid Tangerine",
            "Vivid Vermilion",
            "Vivid Violet",
            "Vivid Yellow",
            "Volt",
            "Wageningen Green",
            "Warm Black",
            "Watermelon",
            "Watermelon Red",
            "Waterspout",
            "Weldon Blue",
            "Wenge",
            "Wheat",
            "White",
            "White Chocolate",
            "White Coffee",
            "White Smoke",
            "Wild Blue Yonder",
            "Wild Orchid",
            "Wild Strawberry",
            "Wild Watermelon",
            "Willpower Orange",
            "Windsor Tan",
            "Wine",
            "Wine Red",
            "Wine Dregs",
            "Winter Sky",
            "Winter Wizard",
            "Wintergreen Dream",
            "Wisteria",
            "Wood Brown",
            "Xanadu",
            "Yale Blue",
            "Yankees Blue",
            "Yellow",
            "Yellow (Crayola)",
            "Yellow (Munsell)",
            "Yellow (NCS)",
            "Yellow (Pantone)",
            "Yellow (Process)",
            "Yellow (RYB)",
            "Yellow-Green",
            "Yellow Orange",
            "Yellow Rose",
            "Yellow Sunshine",
            "Zaffre",
            "Zinnwaldite Brown",
            "Zomp"
    };
    private static int[] wikipediaRawColors = {
            0x00, 0x48, 0xBA,
            0x4C, 0x2F, 0x27,
            0xB0, 0xBF, 0x1A,
            0x7C, 0xB9, 0xE8,
            0xC9, 0xFF, 0xE5,
            0xB2, 0x84, 0xBE,
            0x5D, 0x8A, 0xA8,
            0x00, 0x30, 0x8F,
            0x72, 0xA0, 0xC1,
            0xAF, 0x00, 0x2A,
            0xF2, 0xF0, 0xE6,
            0xF0, 0xF8, 0xFF,
            0x84, 0xDE, 0x02,
            0xE3, 0x26, 0x36,
            0xC4, 0x62, 0x10,
            0xEF, 0xDE, 0xCD,
            0xE5, 0x2B, 0x50,
            0x9F, 0x2B, 0x68,
            0xF1, 0x9C, 0xBB,
            0xAB, 0x27, 0x4F,
            0xD3, 0x21, 0x2D,
            0x3B, 0x7A, 0x57,
            0x00, 0xC4, 0xB0,
            0xFF, 0xBF, 0x00,
            0xFF, 0x7E, 0x00,
            0x3B, 0x3B, 0x6D,
            0x80, 0x40, 0x40,
            0xD3, 0xAF, 0x37,
            0x34, 0xB3, 0x34,
            0xFF, 0x8B, 0x00,
            0xFF, 0x98, 0x99,
            0x43, 0x1C, 0x53,
            0xB3, 0x21, 0x34,
            0xFF, 0x03, 0x3E,
            0xCF, 0xCF, 0xCF,
            0x55, 0x1B, 0x8C,
            0xF2, 0xB4, 0x00,
            0x99, 0x66, 0xCC,
            0xA4, 0xC6, 0x39,
            0xF2, 0xF3, 0xF4,
            0xCD, 0x95, 0x75,
            0x66, 0x5D, 0x1E,
            0x91, 0x5C, 0x83,
            0x84, 0x1B, 0x2D,
            0xFA, 0xEB, 0xD7,
            0x00, 0x80, 0x00,
            0x66, 0xB4, 0x47,
            0x8D, 0xB6, 0x00,
            0xFB, 0xCE, 0xB1,
            0x00, 0xFF, 0xFF,
            0x7F, 0xFF, 0xD4,
            0xD0, 0xFF, 0x14,
            0x4B, 0x53, 0x20,
            0x3B, 0x44, 0x4B,
            0x8F, 0x97, 0x79,
            0xE9, 0xD6, 0x6B,
            0xB2, 0xBE, 0xB5,
            0x87, 0xA9, 0x6B,
            0x00, 0x3A, 0x6C,
            0xFF, 0x99, 0x66,
            0xA5, 0x2A, 0x2A,
            0xFD, 0xEE, 0x00,
            0x6E, 0x7F, 0x80,
            0x56, 0x82, 0x03,
            0xFF, 0x20, 0x52,
            0x63, 0x77, 0x5B,
            0xC3, 0x99, 0x53,
            0x00, 0x7F, 0xFF,
            0xF0, 0xFF, 0xFF,
            0xF0, 0xFF, 0xFF,
            0xDB, 0xE9, 0xF4,
            0x89, 0xCF, 0xF0,
            0xA1, 0xCA, 0xF1,
            0xF4, 0xC2, 0xC2,
            0xFE, 0xFE, 0xFA,
            0xFF, 0x91, 0xAF,
            0x21, 0xAB, 0xCD,
            0xFA, 0xE7, 0xB5,
            0xFF, 0xE1, 0x35,
            0x00, 0x6A, 0x4E,
            0xE0, 0x21, 0x8A,
            0x7C, 0x0A, 0x02,
            0x1D, 0xAC, 0xD6,
            0x84, 0x84, 0x82,
            0x98, 0x77, 0x7B,
            0xBC, 0xD4, 0xE6,
            0x9F, 0x81, 0x70,
            0xFA, 0x6E, 0x79,
            0xF5, 0xF5, 0xDC,
            0x2E, 0x58, 0x94,
            0x9C, 0x25, 0x42,
            0xE8, 0x8E, 0x5A,
            0xFF, 0xE4, 0xC4,
            0x3D, 0x2B, 0x1F,
            0x96, 0x71, 0x17,
            0xCA, 0xE0, 0x0D,
            0xBF, 0xFF, 0x00,
            0xFE, 0x6F, 0x5E,
            0xBF, 0x4F, 0x51,
            0x00, 0x00, 0x00,
            0x3D, 0x0C, 0x02,
            0x1B, 0x18, 0x11,
            0x3B, 0x2F, 0x2F,
            0x54, 0x62, 0x6F,
            0x25, 0x35, 0x29,
            0x3B, 0x3C, 0x36,
            0x8F, 0x59, 0x73,
            0xBF, 0xAF, 0xB2,
            0xFF, 0xEB, 0xCD,
            0xA5, 0x71, 0x64,
            0x31, 0x8C, 0xE7,
            0xAC, 0xE5, 0xEE,
            0xFA, 0xF0, 0xBE,
            0xD1, 0x00, 0x1C,
            0x66, 0x00, 0x00,
            0x00, 0x00, 0xFF,
            0x1F, 0x75, 0xFE,
            0x00, 0x93, 0xAF,
            0x00, 0x87, 0xBD,
            0x00, 0x18, 0xA8,
            0x33, 0x33, 0x99,
            0x02, 0x47, 0xFE,
            0xA2, 0xA2, 0xD0,
            0x00, 0xB9, 0xFB,
            0x66, 0x99, 0xCC,
            0x0D, 0x98, 0xBA,
            0x5D, 0xAD, 0xEC,
            0xAC, 0xE5, 0xEE,
            0x55, 0x35, 0x92,
            0x12, 0x61, 0x80,
            0x8A, 0x2B, 0xE2,
            0x50, 0x72, 0xA7,
            0x4F, 0x86, 0xF7,
            0x1C, 0x1C, 0xF0,
            0xDE, 0x5D, 0x83,
            0x79, 0x44, 0x3B,
            0x00, 0x95, 0xB6,
            0xE3, 0xDA, 0xC9,
            0xDD, 0xE2, 0x6A,
            0xCC, 0x00, 0x00,
            0x00, 0x6A, 0x4E,
            0x87, 0x32, 0x60,
            0x00, 0x70, 0xFF,
            0xB5, 0xA6, 0x42,
            0xCB, 0x41, 0x54,
            0x1D, 0xAC, 0xD6,
            0xEB, 0xEC, 0xF0,
            0x66, 0xFF, 0x00,
            0xBF, 0x94, 0xE4,
            0xD8, 0x91, 0xEF,
            0xC3, 0x21, 0x48,
            0x19, 0x74, 0xD2,
            0xFF, 0x00, 0x7F,
            0x08, 0xE8, 0xDE,
            0xD1, 0x9F, 0xE8,
            0xFF, 0xAA, 0x1D,
            0x33, 0x99, 0xFF,
            0xF4, 0xBB, 0xFF,
            0xFF, 0x55, 0xA3,
            0xFB, 0x60, 0x7F,
            0x00, 0x42, 0x25,
            0x88, 0x54, 0x0B,
            0xCD, 0x7F, 0x32,
            0xB0, 0x8D, 0x57,
            0x73, 0x70, 0x00,
            0x99, 0x33, 0x00,
            0xAF, 0x59, 0x3E,
            0x96, 0x4B, 0x00,
            0xA5, 0x2A, 0x2A,
            0x6B, 0x44, 0x23,
            0xAF, 0x6E, 0x4D,
            0x5F, 0x19, 0x33,
            0x4A, 0x2C, 0x2A,
            0xCC, 0x99, 0x66,
            0x1B, 0x4D, 0x3E,
            0xFF, 0xC1, 0xCC,
            0xE7, 0xFE, 0xFF,
            0x7B, 0xB6, 0x61,
            0xF0, 0xDC, 0x82,
            0x48, 0x06, 0x07,
            0x80, 0x00, 0x20,
            0xDE, 0xB8, 0x87,
            0xA1, 0x7A, 0x74,
            0xCC, 0x55, 0x00,
            0xE9, 0x74, 0x51,
            0x8A, 0x33, 0x24,
            0x24, 0xA0, 0xED,
            0xBD, 0x33, 0xA4,
            0x70, 0x29, 0x63,
            0x53, 0x68, 0x72,
            0x5F, 0x9E, 0xA0,
            0x91, 0xA3, 0xB0,
            0x0A, 0x11, 0x95,
            0x00, 0x6B, 0x3C,
            0xED, 0x87, 0x2D,
            0xB6, 0x0C, 0x26,
            0xE3, 0x00, 0x22,
            0xFF, 0xF6, 0x00,
            0x7F, 0x3E, 0x98,
            0xA6, 0x7B, 0x5B,
            0x4B, 0x36, 0x21,
            0x1E, 0x4D, 0x2B,
            0xFC, 0xFF, 0xA4,
            0xA3, 0xC1, 0xAD,
            0xC1, 0x9A, 0x6B,
            0xEF, 0xBB, 0xCC,
            0x78, 0x86, 0x6B,
            0xFF, 0xFF, 0x99,
            0xFF, 0xEF, 0x00,
            0xFF, 0x08, 0x00,
            0xE4, 0x71, 0x7A,
            0x00, 0xBF, 0xFF,
            0x59, 0x27, 0x20,
            0xFF, 0xD5, 0x9A,
            0xC4, 0x1E, 0x3A,
            0x00, 0xCC, 0x99,
            0x96, 0x00, 0x18,
            0xD7, 0x00, 0x40,
            0xEB, 0x4C, 0x42,
            0xFF, 0x00, 0x38,
            0xFF, 0xA6, 0xC9,
            0xB3, 0x1B, 0x1B,
            0x56, 0xA0, 0xD3,
            0xED, 0x91, 0x21,
            0x00, 0x56, 0x3F,
            0x06, 0x2A, 0x78,
            0x70, 0x36, 0x42,
            0xC9, 0x5A, 0x49,
            0x92, 0xA1, 0xCF,
            0xAC, 0xE1, 0xAF,
            0x00, 0x7B, 0xA7,
            0x2F, 0x84, 0x7C,
            0xB2, 0xFF, 0xFF,
            0x49, 0x97, 0xD0,
            0xDE, 0x31, 0x63,
            0xEC, 0x3B, 0x83,
            0x00, 0x7B, 0xA7,
            0x2A, 0x52, 0xBE,
            0x6D, 0x9B, 0xC3,
            0x00, 0x7A, 0xA5,
            0xE0, 0x3C, 0x31,
            0xA0, 0x78, 0x5A,
            0xF7, 0xE7, 0xCE,
            0xF1, 0xDD, 0xCF,
            0x36, 0x45, 0x4F,
            0x23, 0x2B, 0x2B,
            0xD0, 0x74, 0x8B,
            0xE6, 0x8F, 0xAC,
            0xDF, 0xFF, 0x00,
            0x7F, 0xFF, 0x00,
            0xFF, 0xA6, 0x00,
            0xDE, 0x31, 0x63,
            0xFF, 0xB7, 0xC5,
            0x95, 0x45, 0x35,
            0xDE, 0x6F, 0xA1,
            0xA8, 0x51, 0x6E,
            0x14, 0x14, 0x14,
            0x36, 0x51, 0x94,
            0xCD, 0x80, 0x32,
            0xAB, 0x38, 0x1F,
            0xD0, 0xDB, 0x61,
            0xCC, 0x99, 0x00,
            0xF3, 0x70, 0x42,
            0xDE, 0x70, 0xA1,
            0x72, 0x0B, 0x98,
            0xCD, 0x07, 0x1E,
            0xAA, 0x38, 0x1E,
            0xCC, 0xCC, 0xCC,
            0x85, 0x60, 0x88,
            0xE2, 0xE5, 0xDE,
            0xFF, 0xB2, 0x00,
            0x4A, 0xFF, 0x00,
            0x3C, 0x14, 0x21,
            0x7B, 0x3F, 0x00,
            0xD2, 0x69, 0x1E,
            0x2A, 0x8F, 0xBD,
            0x36, 0x51, 0x94,
            0x5D, 0x2B, 0x2C,
            0x4C, 0x1F, 0x02,
            0x3C, 0x8D, 0x0D,
            0x00, 0x75, 0x02,
            0xCA, 0xA9, 0x06,
            0xFF, 0x66, 0x00,
            0xD5, 0x6C, 0x2B,
            0xFF, 0xCC, 0xCB,
            0xE3, 0x42, 0x85,
            0x66, 0x33, 0x98,
            0x4D, 0x08, 0x4B,
            0xAA, 0x01, 0x14,
            0xB0, 0x1B, 0x2E,
            0xE1, 0xDF, 0xE0,
            0xFF, 0xCC, 0x00,
            0xFE, 0xF2, 0x00,
            0xFF, 0xA7, 0x00,
            0x98, 0x81, 0x7B,
            0xE3, 0x42, 0x34,
            0xD2, 0x69, 0x1E,
            0xCD, 0x60, 0x7E,
            0xE4, 0xD0, 0x0A,
            0x93, 0x37, 0x09,
            0x9F, 0xA9, 0x1F,
            0x7F, 0x17, 0x34,
            0xFB, 0xCC, 0xE7,
            0x00, 0x47, 0xAB,
            0xD2, 0x69, 0x1E,
            0x96, 0x5A, 0x3E,
            0x6F, 0x4E, 0x37,
            0x3C, 0x30, 0x24,
            0xC4, 0xD8, 0xE2,
            0xFF, 0xFF, 0xCC,
            0xF8, 0x83, 0x79,
            0x00, 0x2E, 0x63,
            0x8C, 0x92, 0xAC,
            0xEE, 0xE0, 0xB1,
            0xB8, 0x73, 0x33,
            0xDA, 0x8A, 0x67,
            0xAD, 0x6F, 0x69,
            0xCB, 0x6D, 0x51,
            0x99, 0x66, 0x66,
            0xFF, 0x38, 0x00,
            0xFF, 0x7F, 0x50,
            0xF8, 0x83, 0x79,
            0xFF, 0x40, 0x40,
            0xFD, 0x7C, 0x6E,
            0x89, 0x3F, 0x45,
            0xFB, 0xEC, 0x5D,
            0xB3, 0x1B, 0x1B,
            0x64, 0x95, 0xED,
            0xFF, 0xF8, 0xDC,
            0x2E, 0x2D, 0x88,
            0xFF, 0xF8, 0xE7,
            0x81, 0x61, 0x3C,
            0xFF, 0xBC, 0xD9,
            0xFF, 0xFD, 0xD0,
            0xDC, 0x14, 0x3C,
            0xBE, 0x00, 0x32,
            0x99, 0x00, 0x00,
            0xF5, 0xF5, 0xF5,
            0x00, 0xFF, 0xFF,
            0x4E, 0x82, 0xB4,
            0x46, 0x82, 0xBF,
            0x28, 0x58, 0x9C,
            0x18, 0x8B, 0xC2,
            0x00, 0xB7, 0xEB,
            0x58, 0x42, 0x7C,
            0xFF, 0xD3, 0x00,
            0xF5, 0x6F, 0xA1,
            0xFF, 0xFF, 0x31,
            0xF0, 0xE1, 0x30,
            0x00, 0x00, 0x8B,
            0x66, 0x66, 0x99,
            0x80, 0x4A, 0x00,
            0x65, 0x43, 0x21,
            0x88, 0x65, 0x4E,
            0x5D, 0x39, 0x54,
            0xA4, 0x00, 0x00,
            0x08, 0x45, 0x7E,
            0x33, 0x33, 0x33,
            0x98, 0x69, 0x60,
            0x49, 0x02, 0x06,
            0x3C, 0x13, 0x21,
            0x26, 0x42, 0x8B,
            0xCD, 0x5B, 0x45,
            0x00, 0x8B, 0x8B,
            0x53, 0x68, 0x78,
            0xB8, 0x86, 0x0B,
            0xA9, 0xA9, 0xA9,
            0x01, 0x32, 0x20,
            0x00, 0x64, 0x00,
            0x1F, 0x26, 0x2A,
            0x00, 0x41, 0x6A,
            0x00, 0x14, 0x7E,
            0x1A, 0x24, 0x21,
            0xBD, 0xB7, 0x6B,
            0x48, 0x3C, 0x32,
            0x73, 0x4F, 0x96,
            0x8B, 0xBE, 0x1B,
            0x53, 0x4B, 0x4F,
            0x54, 0x3D, 0x37,
            0x8B, 0x00, 0x8B,
            0xA9, 0xA9, 0xA9,
            0x00, 0x33, 0x66,
            0x4A, 0x5D, 0x23,
            0x55, 0x6B, 0x2F,
            0xFF, 0x8C, 0x00,
            0x99, 0x32, 0xCC,
            0x77, 0x9E, 0xCB,
            0x03, 0xC0, 0x3C,
            0x96, 0x6F, 0xD6,
            0xC2, 0x3B, 0x22,
            0xE7, 0x54, 0x80,
            0x00, 0x33, 0x99,
            0x4F, 0x3A, 0x3C,
            0x30, 0x19, 0x34,
            0x87, 0x26, 0x57,
            0x8B, 0x00, 0x00,
            0xE9, 0x96, 0x7A,
            0x56, 0x03, 0x19,
            0x8F, 0xBC, 0x8F,
            0x3C, 0x14, 0x14,
            0x8C, 0xBE, 0xD6,
            0x48, 0x3D, 0x8B,
            0x2F, 0x4F, 0x4F,
            0x17, 0x72, 0x45,
            0x91, 0x81, 0x51,
            0xFF, 0xA8, 0x12,
            0x48, 0x3C, 0x32,
            0xCC, 0x4E, 0x5C,
            0x00, 0xCE, 0xD1,
            0xD1, 0xBE, 0xA8,
            0x94, 0x00, 0xD3,
            0x9B, 0x87, 0x0C,
            0x00, 0x70, 0x3C,
            0x55, 0x55, 0x55,
            0xD7, 0x0A, 0x53,
            0x9C, 0x8A, 0xA4,
            0x40, 0x82, 0x6D,
            0xA9, 0x20, 0x3E,
            0xEF, 0x30, 0x38,
            0xE9, 0x69, 0x2C,
            0xDA, 0x32, 0x87,
            0xFA, 0xD6, 0xA5,
            0xB9, 0x4E, 0x48,
            0x70, 0x42, 0x41,
            0xC1, 0x54, 0xC1,
            0x05, 0x66, 0x08,
            0x0E, 0x7C, 0x61,
            0x00, 0x4B, 0x49,
            0x33, 0x33, 0x66,
            0xF5, 0xC7, 0x1A,
            0x99, 0x55, 0xBB,
            0xCC, 0x00, 0xCC,
            0x82, 0x00, 0x00,
            0xD4, 0x73, 0xD4,
            0x35, 0x5E, 0x3B,
            0xFF, 0xCB, 0xA4,
            0xFF, 0x14, 0x93,
            0xA9, 0x5C, 0x68,
            0x85, 0x01, 0x01,
            0x84, 0x3F, 0x5B,
            0xFF, 0x99, 0x33,
            0x00, 0xBF, 0xFF,
            0x4A, 0x64, 0x6C,
            0x55, 0x6B, 0x2F,
            0x7E, 0x5E, 0x60,
            0x66, 0x42, 0x4D,
            0x33, 0x00, 0x66,
            0xBA, 0x87, 0x59,
            0x15, 0x60, 0xBD,
            0x22, 0x43, 0xB6,
            0x66, 0x99, 0x99,
            0xC1, 0x9A, 0x6B,
            0xED, 0xC9, 0xAF,
            0xEA, 0x3C, 0x53,
            0xB9, 0xF2, 0xFF,
            0x69, 0x69, 0x69,
            0xC5, 0x31, 0x51,
            0x9B, 0x76, 0x53,
            0xB5, 0x65, 0x1E,
            0xE8, 0xE4, 0xC9,
            0x1E, 0x90, 0xFF,
            0xFE, 0xF6, 0x5B,
            0xD7, 0x18, 0x68,
            0x85, 0xBB, 0x65,
            0x82, 0x8E, 0x84,
            0x66, 0x4C, 0x28,
            0x96, 0x71, 0x17,
            0x00, 0x00, 0x9C,
            0xE5, 0xCC, 0xC9,
            0xEF, 0xDF, 0xBB,
            0xE1, 0xA9, 0x5F,
            0x55, 0x5D, 0x50,
            0xC2, 0xB2, 0x80,
            0x1B, 0x1B, 0x1B,
            0x61, 0x40, 0x51,
            0xF0, 0xEA, 0xD6,
            0x10, 0x34, 0xA6,
            0x7D, 0xF9, 0xFF,
            0xFF, 0x00, 0x3F,
            0x00, 0xFF, 0xFF,
            0x00, 0xFF, 0x00,
            0x6F, 0x00, 0xFF,
            0xF4, 0xBB, 0xFF,
            0xCC, 0xFF, 0x00,
            0xBF, 0x00, 0xFF,
            0x3F, 0x00, 0xFF,
            0x8F, 0x00, 0xFF,
            0xFF, 0xFF, 0x33,
            0x50, 0xC8, 0x78,
            0x04, 0x63, 0x07,
            0x6C, 0x30, 0x82,
            0x1B, 0x4D, 0x3E,
            0xB4, 0x83, 0x95,
            0xAB, 0x4B, 0x52,
            0xCC, 0x47, 0x4B,
            0x56, 0x3C, 0x5C,
            0x96, 0xC8, 0xA2,
            0x44, 0xD7, 0xA8,
            0xC1, 0x9A, 0x6B,
            0x80, 0x18, 0x18,
            0xB5, 0x33, 0x89,
            0xDE, 0x52, 0x85,
            0xF4, 0x00, 0xA1,
            0xE5, 0xAA, 0x70,
            0x4D, 0x5D, 0x53,
            0xFD, 0xD5, 0xB1,
            0x4F, 0x79, 0x42,
            0xFF, 0x28, 0x00,
            0x6C, 0x54, 0x1E,
            0xFF, 0x54, 0x70,
            0xB2, 0x22, 0x22,
            0xCE, 0x20, 0x29,
            0xE9, 0x5C, 0x4B,
            0xE2, 0x58, 0x22,
            0xFC, 0x8E, 0xAC,
            0x6B, 0x44, 0x23,
            0xF7, 0xE9, 0x8E,
            0xEE, 0xDC, 0x82,
            0xFF, 0xE9, 0xD1,
            0xA2, 0x00, 0x6D,
            0xFF, 0xFA, 0xF0,
            0xFF, 0xBF, 0x00,
            0xFF, 0x14, 0x93,
            0xCC, 0xFF, 0x00,
            0xFF, 0x00, 0x4F,
            0x01, 0x44, 0x21,
            0x22, 0x8B, 0x22,
            0xA6, 0x7B, 0x5B,
            0x85, 0x6D, 0x4D,
            0x00, 0x72, 0xBB,
            0xFD, 0x3F, 0x92,
            0x86, 0x60, 0x8E,
            0x9E, 0xFD, 0x38,
            0xD4, 0x73, 0xD4,
            0xFD, 0x6C, 0x9E,
            0x81, 0x14, 0x53,
            0x4E, 0x16, 0x09,
            0xC7, 0x2C, 0x48,
            0xF6, 0x4A, 0x8A,
            0x77, 0xB5, 0xFE,
            0x88, 0x06, 0xCE,
            0xAC, 0x1E, 0x44,
            0xA6, 0xE7, 0xFF,
            0xE9, 0x36, 0xA7,
            0xFF, 0x00, 0xFF,
            0xC1, 0x54, 0xC1,
            0xFF, 0x77, 0xFF,
            0xCC, 0x39, 0x7B,
            0xC7, 0x43, 0x75,
            0xE4, 0x84, 0x00,
            0xCC, 0x66, 0x66,
            0xDC, 0xDC, 0xDC,
            0xE4, 0x9B, 0x0F,
            0x99, 0x66, 0x00,
            0x73, 0x36, 0x35,
            0xFF, 0xDF, 0x46,
            0x00, 0x7F, 0x66,
            0xF8, 0xF8, 0xFF,
            0xB0, 0x5C, 0x52,
            0xFE, 0x5A, 0x1D,
            0xB0, 0x65, 0x00,
            0x60, 0x82, 0xB6,
            0xE6, 0xE8, 0xFA,
            0xAB, 0x92, 0xB3,
            0x00, 0xAB, 0x66,
            0xA5, 0x7C, 0x00,
            0xD4, 0xAF, 0x37,
            0xFF, 0xD7, 0x00,
            0xE6, 0xBE, 0x8A,
            0x85, 0x75, 0x4E,
            0xBD, 0x9B, 0x16,
            0x99, 0x65, 0x15,
            0xFC, 0xC2, 0x00,
            0xFF, 0xDF, 0x00,
            0xDA, 0xA5, 0x20,
            0x67, 0x67, 0x67,
            0xA8, 0xE4, 0xA0,
            0x6F, 0x2D, 0xA8,
            0x80, 0x80, 0x80,
            0xBE, 0xBE, 0xBE,
            0x46, 0x59, 0x45,
            0x8C, 0x92, 0xAC,
            0x00, 0x80, 0x01,
            0x00, 0xFF, 0x00,
            0x1C, 0xAC, 0x78,
            0x00, 0x80, 0x00,
            0x00, 0xA8, 0x77,
            0x00, 0x9F, 0x6B,
            0x00, 0xAD, 0x43,
            0x00, 0xA5, 0x50,
            0x66, 0xB0, 0x32,
            0x11, 0x64, 0xB4,
            0x00, 0x99, 0x66,
            0xA7, 0xF4, 0x32,
            0x6E, 0xAE, 0xA1,
            0xAD, 0xFF, 0x2F,
            0x88, 0x58, 0x18,
            0xA9, 0x9A, 0x86,
            0x00, 0xFF, 0x7F,
            0x2A, 0x34, 0x39,
            0x66, 0x38, 0x54,
            0xEB, 0x61, 0x23,
            0x44, 0x6C, 0xCF,
            0x52, 0x18, 0xFA,
            0xE9, 0xD6, 0x6B,
            0x3F, 0xFF, 0x00,
            0x46, 0xCB, 0x18,
            0xC9, 0x00, 0x16,
            0xDA, 0x91, 0x00,
            0x80, 0x80, 0x00,
            0xFF, 0x7A, 0x00,
            0x96, 0x00, 0x18,
            0xDF, 0x73, 0xFF,
            0xAA, 0x98, 0xA9,
            0xAA, 0x00, 0xBB,
            0xF4, 0x00, 0xA1,
            0xF0, 0xFF, 0xF0,
            0x00, 0x6D, 0xB0,
            0x49, 0x79, 0x6B,
            0xFF, 0x1D, 0xCE,
            0xFF, 0x69, 0xB4,
            0x35, 0x5E, 0x3B,
            0x71, 0xA6, 0xD2,
            0xFC, 0xF7, 0x5E,
            0x71, 0xBC, 0x78,
            0x31, 0x91, 0x77,
            0x60, 0x2F, 0x6B,
            0x00, 0x23, 0x95,
            0x66, 0x02, 0x3C,
            0xED, 0x29, 0x39,
            0xB2, 0xEC, 0x5D,
            0x4C, 0x51, 0x6D,
            0x13, 0x88, 0x08,
            0xCD, 0x5C, 0x5C,
            0xE3, 0xA8, 0x57,
            0x4B, 0x00, 0x82,
            0x09, 0x1F, 0x92,
            0x23, 0x30, 0x67,
            0x4B, 0x00, 0x82,
            0xFF, 0x49, 0x6C,
            0x36, 0x0C, 0xCC,
            0x00, 0x2F, 0xA7,
            0xFF, 0x4F, 0x00,
            0xBA, 0x16, 0x0C,
            0xC0, 0x36, 0x2C,
            0x5A, 0x4F, 0xCF,
            0xB3, 0x44, 0x6C,
            0xF4, 0xF0, 0xEC,
            0xB2, 0xFF, 0xFF,
            0xFF, 0xFF, 0xF0,
            0x3D, 0x32, 0x5D,
            0x41, 0x36, 0x28,
            0x00, 0xA8, 0x6B,
            0x9D, 0x29, 0x33,
            0x26, 0x43, 0x48,
            0x2F, 0x75, 0x32,
            0x5B, 0x32, 0x56,
            0xF8, 0xDE, 0x7E,
            0xD7, 0x3B, 0x3E,
            0xDE, 0x8F, 0x4E,
            0xA5, 0x0B, 0x5E,
            0xDA, 0x61, 0x4E,
            0x44, 0x79, 0x8E,
            0x34, 0x34, 0x34,
            0xBB, 0xD0, 0xC9,
            0xF4, 0xCA, 0x16,
            0x8A, 0xB9, 0xF1,
            0xBD, 0xDA, 0x57,
            0x29, 0xAB, 0x87,
            0x4C, 0xBB, 0x17,
            0x7C, 0x1C, 0x05,
            0x3A, 0xB0, 0x9E,
            0xE8, 0xF4, 0x8C,
            0xC3, 0xB0, 0x91,
            0xF0, 0xE6, 0x8C,
            0x8E, 0xE5, 0x3F,
            0x88, 0x2D, 0x17,
            0xE7, 0x9F, 0xC4,
            0x6B, 0x44, 0x23,
            0x35, 0x42, 0x30,
            0x51, 0x28, 0x88,
            0xE8, 0x00, 0x0D,
            0x08, 0x78, 0x30,
            0xD6, 0xCA, 0xDD,
            0x26, 0x61, 0x9C,
            0xFF, 0xFF, 0x66,
            0xA9, 0xBA, 0x9D,
            0xCF, 0x10, 0x20,
            0xB5, 0x7E, 0xDC,
            0xE6, 0xE6, 0xFA,
            0xCC, 0xCC, 0xFF,
            0xFF, 0xF0, 0xF5,
            0xC4, 0xC3, 0xD0,
            0x94, 0x57, 0xEB,
            0xEE, 0x82, 0xEE,
            0xE6, 0xE6, 0xFA,
            0xFB, 0xAE, 0xD2,
            0x96, 0x7B, 0xB6,
            0xFB, 0xA0, 0xE3,
            0x7C, 0xFC, 0x00,
            0xFF, 0xF7, 0x00,
            0xFF, 0xFA, 0xCD,
            0xCC, 0xA0, 0x1D,
            0xFD, 0xFF, 0x00,
            0xE3, 0xFF, 0x00,
            0xF6, 0xEA, 0xBE,
            0xFF, 0xF4, 0x4F,
            0xFF, 0xFF, 0x9F,
            0xBA, 0x93, 0xD8,
            0x54, 0x5A, 0xA7,
            0x1A, 0x11, 0x10,
            0xFD, 0xD5, 0xB1,
            0xAD, 0xD8, 0xE6,
            0xFE, 0x2E, 0x2E,
            0xB5, 0x65, 0x1D,
            0xE6, 0x67, 0x71,
            0x88, 0xAC, 0xE0,
            0xF0, 0x80, 0x80,
            0x93, 0xCC, 0xEA,
            0xF5, 0x69, 0x91,
            0xE0, 0xFF, 0xFF,
            0xFF, 0x5C, 0xCD,
            0xC8, 0xAD, 0x7F,
            0xF9, 0x84, 0xEF,
            0xB2, 0x97, 0x00,
            0xFA, 0xFA, 0xD2,
            0xD3, 0xD3, 0xD3,
            0xCC, 0x99, 0xCC,
            0x90, 0xEE, 0x90,
            0xFF, 0xB3, 0xDE,
            0xF0, 0xE6, 0x8C,
            0xD3, 0x9B, 0xCB,
            0xAD, 0xDF, 0xAD,
            0xFE, 0xD8, 0xB1,
            0xE6, 0xA8, 0xD7,
            0xB1, 0x9C, 0xD9,
            0xC5, 0xCB, 0xE1,
            0xFF, 0xB6, 0xC1,
            0xFF, 0xCC, 0xCB,
            0xE9, 0x74, 0x51,
            0xFF, 0xA0, 0x7A,
            0xFF, 0x99, 0x99,
            0x20, 0xB2, 0xAA,
            0xD8, 0xD8, 0xD8,
            0x87, 0xCE, 0xFA,
            0x77, 0x88, 0x99,
            0xB0, 0xC4, 0xDE,
            0xB3, 0x8B, 0x6D,
            0xE6, 0x8F, 0xAC,
            0xFF, 0xFF, 0xE0,
            0xC8, 0xA2, 0xC8,
            0xAE, 0x98, 0xAA,
            0xBF, 0xFF, 0x00,
            0x00, 0xFF, 0x00,
            0x32, 0xCD, 0x32,
            0x9D, 0xC2, 0x09,
            0x19, 0x59, 0x05,
            0xFA, 0xF0, 0xE6,
            0xC1, 0x9A, 0x6B,
            0xDE, 0x6F, 0xA1,
            0x6C, 0xA0, 0xDC,
            0xF8, 0xB9, 0xD4,
            0x67, 0x4C, 0x47,
            0xB8, 0x6D, 0x29,
            0x6C, 0x2E, 0x1F,
            0x98, 0x74, 0x56,
            0x66, 0x99, 0xCC,
            0x15, 0xF2, 0xFD,
            0xFE, 0xFD, 0xFA,
            0xFF, 0xE4, 0xCD,
            0xE6, 0x20, 0x20,
            0x00, 0x1C, 0x3D,
            0xFF, 0xBD, 0x88,
            0xCC, 0x33, 0x36,
            0xFF, 0x00, 0xFF,
            0xFF, 0x55, 0xA3,
            0xCA, 0x1F, 0x7B,
            0xD0, 0x41, 0x7E,
            0xFF, 0x00, 0x90,
            0x9F, 0x45, 0x76,
            0xCC, 0x33, 0x8B,
            0xAA, 0xF0, 0xD1,
            0xFF, 0x44, 0x66,
            0xF8, 0xF4, 0xFF,
            0xC0, 0x40, 0x00,
            0xFB, 0xEC, 0x5D,
            0xF2, 0xC6, 0x49,
            0x60, 0x50, 0xDC,
            0x0B, 0xDA, 0x51,
            0x97, 0x9A, 0xAA,
            0xF3, 0x7A, 0x48,
            0x96, 0xFF, 0x00,
            0xFF, 0x82, 0x43,
            0x96, 0xFF, 0x00,
            0x74, 0xC3, 0x65,
            0x88, 0x00, 0x85,
            0xEA, 0xA2, 0x21,
            0xC3, 0x21, 0x48,
            0x80, 0x00, 0x00,
            0xB0, 0x30, 0x60,
            0xE0, 0xB0, 0xFF,
            0x91, 0x5F, 0x6D,
            0xEF, 0x98, 0xAA,
            0x47, 0xAB, 0xCC,
            0x30, 0xBF, 0xBF,
            0xAC, 0xAC, 0xE6,
            0x5E, 0x8C, 0x31,
            0xD9, 0xE6, 0x50,
            0x73, 0x33, 0x80,
            0xD9, 0x21, 0x21,
            0xA6, 0x3A, 0x79,
            0xFA, 0xFA, 0x37,
            0xF2, 0xBA, 0x49,
            0x4C, 0x91, 0x41,
            0x73, 0xC2, 0xFB,
            0xE5, 0xB7, 0x3B,
            0x66, 0xDD, 0xAA,
            0x00, 0x00, 0xCD,
            0xE2, 0x06, 0x2C,
            0xAF, 0x40, 0x35,
            0xF3, 0xE5, 0xAB,
            0x03, 0x50, 0x96,
            0x1C, 0x35, 0x2D,
            0xDD, 0xA0, 0xDD,
            0xBA, 0x55, 0xD3,
            0x00, 0x67, 0xA5,
            0x93, 0x70, 0xDB,
            0xBB, 0x33, 0x85,
            0xAA, 0x40, 0x69,
            0x3C, 0xB3, 0x71,
            0x80, 0xDA, 0xEB,
            0x7B, 0x68, 0xEE,
            0xC9, 0xDC, 0x87,
            0x00, 0xFA, 0x9A,
            0x67, 0x4C, 0x47,
            0x48, 0xD1, 0xCC,
            0x79, 0x44, 0x3B,
            0xD9, 0x60, 0x3B,
            0xC7, 0x15, 0x85,
            0xF8, 0xB8, 0x78,
            0xF8, 0xDE, 0x7E,
            0xFD, 0xBC, 0xB4,
            0xC1, 0xF9, 0xA2,
            0x32, 0x52, 0x7B,
            0xA9, 0x71, 0x42,
            0xAC, 0x43, 0x13,
            0xA9, 0x71, 0x42,
            0x29, 0x6E, 0x01,
            0xDA, 0x68, 0x0F,
            0xED, 0xA6, 0xC4,
            0xA6, 0x2C, 0x2B,
            0x0A, 0x7E, 0x8C,
            0xA8, 0xA9, 0xAD,
            0x9C, 0x7C, 0x38,
            0x5B, 0x0A, 0x91,
            0xFD, 0xCC, 0x0D,
            0xE4, 0x00, 0x7C,
            0x7E, 0xD4, 0xE6,
            0x8D, 0xD9, 0xCC,
            0x8B, 0x72, 0xBE,
            0x8B, 0x86, 0x80,
            0x4D, 0x8C, 0x57,
            0xAC, 0xBF, 0x60,
            0xD9, 0x82, 0xB5,
            0xE5, 0x8E, 0x73,
            0xA5, 0x53, 0x53,
            0xFF, 0xEB, 0x00,
            0xEC, 0xB1, 0x76,
            0x70, 0x26, 0x70,
            0x19, 0x19, 0x70,
            0x00, 0x46, 0x8C,
            0x00, 0x49, 0x53,
            0xFF, 0xC4, 0x0C,
            0xFD, 0xFF, 0xF5,
            0x84, 0x56, 0x3C,
            0xFF, 0xDA, 0xE9,
            0xE3, 0xF9, 0x88,
            0x36, 0x74, 0x7D,
            0xF5, 0xE0, 0x50,
            0x3E, 0xB4, 0x89,
            0xF5, 0xFF, 0xFA,
            0x98, 0xFF, 0x98,
            0xBB, 0xB4, 0x77,
            0xFF, 0xE4, 0xE1,
            0xFA, 0xEB, 0xD7,
            0x96, 0x71, 0x17,
            0x3A, 0xA8, 0xC1,
            0x73, 0xA9, 0xC2,
            0xAE, 0x0C, 0x00,
            0x8D, 0xA3, 0x99,
            0x8A, 0x9A, 0x5B,
            0x30, 0xBA, 0x8F,
            0x99, 0x7A, 0x8D,
            0x18, 0x45, 0x3B,
            0x70, 0x54, 0x3E,
            0x30, 0x60, 0x30,
            0xC5, 0x4B, 0x8C,
            0xC8, 0x50, 0x9B,
            0x82, 0x8E, 0x84,
            0xFF, 0xDB, 0x58,
            0xCD, 0x7A, 0x00,
            0x6E, 0x6E, 0x30,
            0xE1, 0xAD, 0x01,
            0x31, 0x78, 0x73,
            0xD6, 0x52, 0x82,
            0xAD, 0x43, 0x79,
            0xFF, 0x55, 0x00,
            0xF6, 0xAD, 0xC6,
            0x2A, 0x80, 0x00,
            0xFA, 0xDA, 0x5E,
            0xFF, 0xDE, 0xAD,
            0x00, 0x00, 0x80,
            0x00, 0x00, 0x80,
            0x19, 0x74, 0xD2,
            0x94, 0x57, 0xEB,
            0x1B, 0x03, 0xA3,
            0xC3, 0x73, 0x2A,
            0xFF, 0xA3, 0x43,
            0x00, 0xFE, 0xFC,
            0xFE, 0x41, 0x64,
            0xCF, 0xAA, 0x01,
            0x80, 0x80, 0x80,
            0x39, 0xFF, 0x14,
            0x39, 0xFF, 0x14,
            0xFE, 0x34, 0x7E,
            0x94, 0x57, 0xEB,
            0xFF, 0x18, 0x18,
            0xFF, 0x26, 0x03,
            0xCC, 0xCC, 0xCC,
            0xF6, 0x89, 0x0A,
            0xFF, 0xF7, 0x00,
            0x21, 0x4F, 0xC6,
            0xD7, 0x83, 0x7F,
            0x72, 0x74, 0x72,
            0xA4, 0xDD, 0xED,
            0x05, 0x90, 0x33,
            0xE9, 0xFF, 0xDB,
            0x4F, 0x42, 0xB5,
            0x00, 0x77, 0xBE,
            0x48, 0xBF, 0x91,
            0xCC, 0x77, 0x22,
            0x00, 0x80, 0x00,
            0xFD, 0x52, 0x40,
            0x43, 0x30, 0x2E,
            0xCF, 0xB5, 0x3B,
            0x56, 0x3C, 0x5C,
            0xFD, 0xF5, 0xE6,
            0x79, 0x68, 0x78,
            0x67, 0x31, 0x47,
            0x86, 0x7E, 0x36,
            0xC0, 0x80, 0x81,
            0x84, 0x84, 0x82,
            0x80, 0x80, 0x00,
            0x6B, 0x8E, 0x23,
            0x3C, 0x34, 0x1F,
            0x9A, 0xB9, 0x73,
            0x35, 0x38, 0x39,
            0xA8, 0xC3, 0xBC,
            0xB7, 0x84, 0xA7,
            0xFF, 0x7F, 0x00,
            0xFF, 0x75, 0x38,
            0xFF, 0x58, 0x00,
            0xFB, 0x99, 0x02,
            0xFF, 0xA5, 0x00,
            0xFF, 0x9F, 0x00,
            0xFF, 0x45, 0x00,
            0xFA, 0x5B, 0x3D,
            0xF8, 0xD5, 0x68,
            0xDA, 0x70, 0xD6,
            0xF2, 0xBD, 0xCD,
            0xFB, 0x4F, 0x14,
            0x65, 0x43, 0x21,
            0x41, 0x4A, 0x4C,
            0xFF, 0x6E, 0x4A,
            0x00, 0x21, 0x47,
            0x6D, 0x9A, 0x79,
            0x99, 0x00, 0x00,
            0x1C, 0xA9, 0xC9,
            0x00, 0x66, 0x00,
            0x27, 0x3B, 0xE2,
            0x68, 0x28, 0x60,
            0xBC, 0xD4, 0xE6,
            0xAF, 0xEE, 0xEE,
            0x98, 0x76, 0x54,
            0xAF, 0x40, 0x35,
            0x9B, 0xC4, 0xE2,
            0xDD, 0xAD, 0xAF,
            0xDA, 0x8A, 0x67,
            0xAB, 0xCD, 0xEF,
            0x87, 0xD3, 0xF8,
            0xE6, 0xBE, 0x8A,
            0xEE, 0xE8, 0xAA,
            0x98, 0xFB, 0x98,
            0xDC, 0xD0, 0xFF,
            0xF9, 0x84, 0xE5,
            0xFF, 0x99, 0xCC,
            0xFA, 0xDA, 0xDD,
            0xDD, 0xA0, 0xDD,
            0xDB, 0x70, 0x93,
            0x96, 0xDE, 0xD1,
            0xC9, 0xC0, 0xBB,
            0xEC, 0xEB, 0xBD,
            0xBC, 0x98, 0x7E,
            0xAF, 0xEE, 0xEE,
            0xCC, 0x99, 0xFF,
            0xDB, 0x70, 0x93,
            0x6F, 0x99, 0x40,
            0x78, 0x18, 0x4A,
            0x00, 0x9B, 0x7D,
            0xFF, 0xEF, 0xD5,
            0xE6, 0x3E, 0x62,
            0x50, 0xC8, 0x78,
            0xD9, 0x98, 0xA0,
            0xAE, 0xC6, 0xCF,
            0x83, 0x69, 0x53,
            0xCF, 0xCF, 0xC4,
            0x77, 0xDD, 0x77,
            0xF4, 0x9A, 0xC2,
            0xFF, 0xB3, 0x47,
            0xDE, 0xA5, 0xA4,
            0xB3, 0x9E, 0xB5,
            0xFF, 0x69, 0x61,
            0xCB, 0x99, 0xC9,
            0xFD, 0xFD, 0x96,
            0x80, 0x00, 0x80,
            0x53, 0x68, 0x78,
            0xFF, 0xE5, 0xB4,
            0xFF, 0xCB, 0xA4,
            0xFF, 0xCC, 0x99,
            0xFF, 0xDA, 0xB9,
            0xFA, 0xDF, 0xAD,
            0xD1, 0xE2, 0x31,
            0xEA, 0xE0, 0xC8,
            0x88, 0xD8, 0xC0,
            0xB7, 0x68, 0xA2,
            0xE6, 0xE2, 0x00,
            0xCC, 0xCC, 0xFF,
            0xC3, 0xCD, 0xE6,
            0xE1, 0x2C, 0x2C,
            0x1C, 0x39, 0xBB,
            0x00, 0xA6, 0x93,
            0x32, 0x12, 0x7A,
            0xD9, 0x90, 0x58,
            0xF7, 0x7F, 0xBE,
            0x70, 0x1C, 0x1C,
            0xCC, 0x33, 0x33,
            0xFE, 0x28, 0xA2,
            0xEC, 0x58, 0x00,
            0xCD, 0x85, 0x3F,
            0x8B, 0xA8, 0xB7,
            0x00, 0x38, 0xA7,
            0x5D, 0x19, 0x16,
            0xB1, 0x73, 0x04,
            0xFF, 0xDF, 0x00,
            0x8C, 0x8C, 0x8C,
            0x00, 0x85, 0x43,
            0xFF, 0x73, 0x00,
            0xFA, 0x1A, 0x8E,
            0xCE, 0x11, 0x27,
            0xA5, 0x7C, 0x00,
            0xCE, 0x11, 0x27,
            0xFE, 0xCB, 0x00,
            0xDF, 0x00, 0xFF,
            0x00, 0x0F, 0x89,
            0x12, 0x35, 0x24,
            0x45, 0xB1, 0xE8,
            0xC3, 0x0B, 0x4E,
            0xFD, 0xDD, 0xE6,
            0x01, 0x79, 0x6F,
            0x2A, 0x2F, 0x23,
            0x56, 0x3C, 0x0D,
            0xFF, 0xC0, 0xCB,
            0xD7, 0x48, 0x94,
            0xFC, 0x74, 0xFD,
            0xFF, 0xDD, 0xF4,
            0xD8, 0xB2, 0xD1,
            0xFF, 0x99, 0x66,
            0xE7, 0xAC, 0xCF,
            0x98, 0x00, 0x36,
            0xF7, 0x8F, 0xA7,
            0x93, 0xC5, 0x72,
            0x39, 0x12, 0x85,
            0xE5, 0xE4, 0xE2,
            0x8E, 0x45, 0x85,
            0xDD, 0xA0, 0xDD,
            0x59, 0x46, 0xB2,
            0x37, 0x4F, 0x6B,
            0x5D, 0xA4, 0x93,
            0x86, 0x60, 0x8E,
            0xBE, 0x4F, 0x62,
            0xFF, 0x5A, 0x36,
            0xB0, 0xE0, 0xE6,
            0xFF, 0x85, 0xCF,
            0xF5, 0x80, 0x25,
            0x70, 0x1C, 0x1C,
            0x00, 0x31, 0x53,
            0xDF, 0x00, 0xFF,
            0xCC, 0x88, 0x99,
            0x72, 0x2F, 0x37,
            0x64, 0x41, 0x17,
            0x3B, 0x33, 0x1C,
            0xFF, 0x75, 0x18,
            0x80, 0x00, 0x80,
            0x9F, 0x00, 0xC5,
            0xA0, 0x20, 0xF0,
            0x69, 0x35, 0x9C,
            0x96, 0x78, 0xB6,
            0x4E, 0x51, 0x80,
            0xFE, 0x4E, 0xDA,
            0x9C, 0x51, 0xB6,
            0x50, 0x40, 0x4D,
            0x9A, 0x4E, 0xAE,
            0x51, 0x48, 0x4F,
            0x43, 0x6B, 0x95,
            0xE8, 0xCC, 0xD7,
            0xA6, 0xA6, 0xA6,
            0x8E, 0x3A, 0x59,
            0x6A, 0x54, 0x45,
            0x5D, 0x8A, 0xA8,
            0xFF, 0x35, 0x5E,
            0x24, 0x21, 0x24,
            0xFB, 0xAB, 0x60,
            0xE3, 0x0B, 0x5D,
            0x91, 0x5F, 0x6D,
            0xE2, 0x50, 0x98,
            0xB3, 0x44, 0x6C,
            0xD6, 0x8A, 0x59,
            0x82, 0x66, 0x44,
            0xFF, 0x33, 0xCC,
            0xE3, 0x25, 0x6B,
            0x8D, 0x4E, 0x85,
            0x66, 0x33, 0x99,
            0xFF, 0x00, 0x00,
            0xEE, 0x20, 0x4D,
            0xF2, 0x00, 0x3C,
            0xC4, 0x02, 0x33,
            0xED, 0x29, 0x39,
            0xED, 0x1C, 0x24,
            0xFE, 0x27, 0x12,
            0xA5, 0x2A, 0x2A,
            0x86, 0x01, 0x11,
            0xFF, 0x53, 0x49,
            0xE4, 0x00, 0x78,
            0xFD, 0x3A, 0x4A,
            0xC7, 0x15, 0x85,
            0xA4, 0x5A, 0x52,
            0x52, 0x2D, 0x80,
            0x00, 0x00, 0x00,
            0x00, 0x23, 0x87,
            0x77, 0x76, 0x96,
            0x00, 0x40, 0x40,
            0x01, 0x0B, 0x13,
            0x01, 0x02, 0x03,
            0xF1, 0xA7, 0xFE,
            0xD7, 0x00, 0x40,
            0x08, 0x92, 0xD0,
            0xA7, 0x6B, 0xCF,
            0xB6, 0x66, 0xD2,
            0xB0, 0x30, 0x60,
            0x44, 0x4C, 0x38,
            0x70, 0x42, 0x41,
            0x00, 0xCC, 0xCC,
            0x8A, 0x7F, 0x80,
            0x83, 0x89, 0x96,
            0x29, 0x0E, 0x05,
            0xFF, 0x00, 0x7F,
            0xF9, 0x42, 0x9E,
            0x9E, 0x5E, 0x6F,
            0x67, 0x48, 0x46,
            0x96, 0x01, 0x45,
            0xB7, 0x6E, 0x79,
            0xE3, 0x26, 0x36,
            0xFF, 0x66, 0xCC,
            0xAA, 0x98, 0xA9,
            0xBD, 0x55, 0x9C,
            0xC2, 0x1E, 0x56,
            0x90, 0x5D, 0x5D,
            0xAB, 0x4E, 0x52,
            0x65, 0x00, 0x0B,
            0xD4, 0x00, 0x00,
            0xBC, 0x8F, 0x8F,
            0x00, 0x38, 0xA8,
            0x00, 0x23, 0x66,
            0x41, 0x69, 0xE1,
            0x52, 0x3B, 0x35,
            0xCA, 0x2C, 0x92,
            0x13, 0x62, 0x07,
            0xF9, 0x92, 0x45,
            0x9B, 0x1C, 0x31,
            0x9B, 0x1C, 0x31,
            0xD0, 0x00, 0x60,
            0x78, 0x51, 0xA9,
            0xFA, 0xDA, 0x5E,
            0xCE, 0x46, 0x76,
            0xD1, 0x00, 0x56,
            0xE0, 0x11, 0x5F,
            0x9B, 0x11, 0x1E,
            0xFF, 0x00, 0x28,
            0xBB, 0x65, 0x28,
            0xE1, 0x8E, 0x96,
            0xA8, 0x1C, 0x07,
            0x80, 0x46, 0x1B,
            0x67, 0x92, 0x67,
            0x32, 0x17, 0x4D,
            0xB7, 0x41, 0x0E,
            0xDA, 0x2C, 0x43,
            0x00, 0x56, 0x3F,
            0x8B, 0x45, 0x13,
            0xFF, 0x78, 0x00,
            0xFF, 0x67, 0x00,
            0xEE, 0xD2, 0x02,
            0xF4, 0xC4, 0x30,
            0xBC, 0xB8, 0x8A,
            0x23, 0x29, 0x7A,
            0x17, 0x7B, 0x4D,
            0xFA, 0x80, 0x72,
            0xFF, 0x91, 0xA4,
            0xC2, 0xB2, 0x80,
            0x96, 0x71, 0x17,
            0xEC, 0xD5, 0x40,
            0xF4, 0xA4, 0x60,
            0xFD, 0xD9, 0xB5,
            0x96, 0x71, 0x17,
            0x92, 0x00, 0x0A,
            0x50, 0x7D, 0x2A,
            0x0F, 0x52, 0xBA,
            0x00, 0x67, 0xA5,
            0xFF, 0x46, 0x81,
            0xCB, 0xA1, 0x35,
            0xFF, 0x24, 0x00,
            0xFD, 0x0E, 0x35,
            0xFF, 0x91, 0xAF,
            0xFF, 0xD8, 0x00,
            0x66, 0xFF, 0x66,
            0x00, 0x69, 0x94,
            0x9F, 0xE2, 0xBF,
            0x2E, 0x8B, 0x57,
            0x4B, 0xC7, 0xCF,
            0x59, 0x26, 0x0B,
            0xFF, 0xF5, 0xEE,
            0xFF, 0xBA, 0x00,
            0x70, 0x42, 0x14,
            0x8A, 0x79, 0x5D,
            0x77, 0x8B, 0xA5,
            0xFF, 0xCF, 0xF1,
            0x00, 0x9E, 0x60,
            0x8F, 0xD4, 0x00,
            0xD9, 0x86, 0x95,
            0x5F, 0xA7, 0x78,
            0xFC, 0x0F, 0xC0,
            0xFF, 0x6F, 0xFF,
            0x88, 0x2D, 0x17,
            0xC0, 0xC0, 0xC0,
            0xC9, 0xC0, 0xBB,
            0xAA, 0xA9, 0xAD,
            0xAC, 0xAC, 0xAC,
            0xAF, 0xB1, 0xAE,
            0x5D, 0x89, 0xBA,
            0xC4, 0xAE, 0xAD,
            0xBF, 0xC1, 0xC2,
            0xCB, 0x41, 0x0B,
            0xFF, 0x38, 0x55,
            0xFF, 0xDB, 0x00,
            0x00, 0x74, 0x74,
            0x87, 0xCE, 0xEB,
            0x76, 0xD7, 0xEA,
            0xCF, 0x71, 0xAF,
            0x6A, 0x5A, 0xCD,
            0x70, 0x80, 0x90,
            0x29, 0x96, 0x17,
            0x00, 0x33, 0x99,
            0xFF, 0x6D, 0x3A,
            0xC8, 0x41, 0x86,
            0x73, 0x82, 0x76,
            0x83, 0x2A, 0x0D,
            0x10, 0x0C, 0x08,
            0x93, 0x3D, 0x41,
            0xFF, 0xFA, 0xFA,
            0xCE, 0xC8, 0xEF,
            0x54, 0x5A, 0x2C,
            0x89, 0x38, 0x43,
            0x75, 0x75, 0x75,
            0x9E, 0x13, 0x16,
            0x1D, 0x29, 0x51,
            0x80, 0x75, 0x32,
            0x00, 0x70, 0xB8,
            0xD1, 0x00, 0x47,
            0xE5, 0x1A, 0x4C,
            0x98, 0x98, 0x98,
            0x00, 0x91, 0x50,
            0xE8, 0x61, 0x00,
            0xF7, 0xBF, 0xBE,
            0x66, 0x03, 0x3C,
            0xE6, 0x00, 0x26,
            0x00, 0xFF, 0xFF,
            0x4C, 0x28, 0x82,
            0x00, 0x7F, 0x5C,
            0xF6, 0xB5, 0x11,
            0x8B, 0x5F, 0x4D,
            0x0F, 0xC0, 0xFC,
            0xA7, 0xFC, 0x00,
            0x87, 0xFF, 0x2A,
            0x00, 0xFF, 0x7F,
            0xEC, 0xEB, 0xBD,
            0x00, 0x7B, 0xB8,
            0x46, 0x82, 0xB4,
            0xCC, 0x33, 0xCC,
            0x5F, 0x8A, 0x8B,
            0xFA, 0xDA, 0x5E,
            0x99, 0x00, 0x00,
            0x4F, 0x66, 0x6A,
            0xE4, 0xD9, 0x6F,
            0xFC, 0x5A, 0x8D,
            0x91, 0x4E, 0x75,
            0xFF, 0x40, 0x4C,
            0xFF, 0xCC, 0x33,
            0xF2, 0xF2, 0x7A,
            0xE3, 0xAB, 0x57,
            0xFA, 0xD6, 0xA5,
            0xFD, 0x5E, 0x53,
            0xCF, 0x6B, 0xA9,
            0xA8, 0x37, 0x31,
            0xD2, 0xB4, 0x8C,
            0xF9, 0x4D, 0x00,
            0xF2, 0x85, 0x00,
            0xFF, 0xCC, 0x00,
            0xE4, 0x71, 0x7A,
            0xFB, 0x4D, 0x46,
            0x48, 0x3C, 0x32,
            0x8B, 0x85, 0x89,
            0xD0, 0xF0, 0xC0,
            0xF8, 0x83, 0x79,
            0xF4, 0xC2, 0xC2,
            0x00, 0x80, 0x80,
            0x36, 0x75, 0x88,
            0x99, 0xE6, 0xB3,
            0x00, 0x82, 0x7F,
            0xCF, 0x34, 0x76,
            0x3C, 0x21, 0x26,
            0xCD, 0x57, 0x00,
            0xE2, 0x72, 0x5B,
            0xD8, 0xBF, 0xD8,
            0xDE, 0x6F, 0xA1,
            0xFC, 0x89, 0xAC,
            0x0A, 0xBA, 0xB5,
            0xE0, 0x8D, 0x3C,
            0xDB, 0xD7, 0xD2,
            0x87, 0x86, 0x81,
            0xEE, 0xE6, 0x00,
            0xFF, 0x63, 0x47,
            0x74, 0x6C, 0xC0,
            0xFF, 0xC8, 0x7C,
            0xFD, 0x0E, 0x35,
            0x80, 0x80, 0x80,
            0x00, 0x75, 0x5E,
            0xCD, 0xA4, 0xDE,
            0x00, 0x73, 0xCF,
            0x3E, 0x8E, 0xDE,
            0xFF, 0x87, 0x8D,
            0xDE, 0xAA, 0x88,
            0xB5, 0x72, 0x81,
            0x40, 0xE0, 0xD0,
            0x00, 0xFF, 0xEF,
            0xA0, 0xD6, 0xB4,
            0x00, 0xC5, 0xCD,
            0x8A, 0x9A, 0x5B,
            0xFA, 0xD6, 0xA5,
            0x6F, 0x4E, 0x37,
            0x7C, 0x48, 0x48,
            0xA6, 0x7B, 0x5B,
            0xC0, 0x99, 0x99,
            0x8A, 0x49, 0x6B,
            0x66, 0x02, 0x3C,
            0x00, 0x33, 0xAA,
            0xD9, 0x00, 0x4C,
            0x88, 0x78, 0xC3,
            0x53, 0x68, 0x95,
            0xFF, 0xB3, 0x00,
            0xBA, 0x00, 0x01,
            0x3C, 0xD0, 0x70,
            0x3F, 0x00, 0xFF,
            0x41, 0x66, 0xF5,
            0xFF, 0x6F, 0xFF,
            0xFC, 0x6C, 0x85,
            0x63, 0x51, 0x47,
            0xFF, 0xDD, 0xCA,
            0x5B, 0x92, 0xE5,
            0xB7, 0x87, 0x27,
            0xF7, 0x7F, 0x00,
            0xFF, 0xFF, 0x66,
            0x01, 0x44, 0x21,
            0x7B, 0x11, 0x13,
            0xAE, 0x20, 0x29,
            0xE1, 0xAD, 0x21,
            0x00, 0x4F, 0x98,
            0x99, 0x00, 0x00,
            0xFF, 0xCC, 0x00,
            0xD3, 0x00, 0x3F,
            0x08, 0x08, 0x08,
            0x66, 0x42, 0x28,
            0xF3, 0xE5, 0xAB,
            0xF3, 0x8F, 0xA9,
            0xC5, 0xB3, 0x58,
            0xC8, 0x08, 0x15,
            0x43, 0xB3, 0xAE,
            0xE3, 0x42, 0x34,
            0xD9, 0x38, 0x1E,
            0xA0, 0x20, 0xF0,
            0x18, 0x88, 0x0d,
            0x74, 0xBB, 0xFB,
            0x66, 0x66, 0xFF,
            0x64, 0xE9, 0x86,
            0xFF, 0xB0, 0x77,
            0xFF, 0xDF, 0xBF,
            0xFF, 0xFF, 0xBF,
            0x8F, 0x00, 0xFF,
            0x32, 0x4A, 0xB2,
            0xF7, 0x53, 0x94,
            0x67, 0x44, 0x03,
            0x40, 0x82, 0x6D,
            0x00, 0x96, 0x98,
            0x7C, 0x9E, 0xD9,
            0xCC, 0x99, 0x00,
            0x92, 0x27, 0x24,
            0x9F, 0x1D, 0x35,
            0xDA, 0x1D, 0x81,
            0x00, 0xAA, 0xEE,
            0xCC, 0x00, 0x33,
            0xFF, 0x99, 0x00,
            0xA6, 0xD6, 0x08,
            0x00, 0xCC, 0x33,
            0xB8, 0x0C, 0xE3,
            0xFF, 0x5F, 0x00,
            0xFF, 0xA0, 0x00,
            0xCC, 0x00, 0xFF,
            0xFF, 0x00, 0x6C,
            0xF7, 0x0D, 0x1A,
            0xDF, 0x61, 0x24,
            0x00, 0xCC, 0xFF,
            0xF0, 0x74, 0x27,
            0xFF, 0xA0, 0x89,
            0xE5, 0x60, 0x24,
            0x9F, 0x00, 0xFF,
            0xFF, 0xE3, 0x02,
            0xCE, 0xFF, 0x00,
            0x34, 0xB2, 0x33,
            0x00, 0x42, 0x42,
            0xF0, 0x5C, 0x85,
            0xBF, 0x41, 0x47,
            0xA4, 0xF4, 0xF9,
            0x7C, 0x98, 0xAB,
            0x64, 0x54, 0x52,
            0xF5, 0xDE, 0xB3,
            0xFF, 0xFF, 0xFF,
            0xED, 0xE6, 0xD6,
            0xE6, 0xE0, 0xD4,
            0xF5, 0xF5, 0xF5,
            0xA2, 0xAD, 0xD0,
            0xD4, 0x70, 0xA2,
            0xFF, 0x43, 0xA4,
            0xFC, 0x6C, 0x85,
            0xFD, 0x58, 0x00,
            0xA7, 0x55, 0x02,
            0x72, 0x2F, 0x37,
            0xB1, 0x12, 0x26,
            0x67, 0x31, 0x47,
            0xFF, 0x00, 0x7C,
            0xA0, 0xE6, 0xFF,
            0x56, 0x88, 0x7D,
            0xC9, 0xA0, 0xDC,
            0xC1, 0x9A, 0x6B,
            0x73, 0x86, 0x78,
            0x0F, 0x4D, 0x92,
            0x1C, 0x28, 0x41,
            0xFF, 0xFF, 0x00,
            0xFC, 0xE8, 0x83,
            0xEF, 0xCC, 0x00,
            0xFF, 0xD3, 0x00,
            0xFE, 0xDF, 0x00,
            0xFF, 0xEF, 0x00,
            0xFE, 0xFE, 0x33,
            0x9A, 0xCD, 0x32,
            0xFF, 0xAE, 0x42,
            0xFF, 0xF0, 0x00,
            0xFF, 0xF7, 0x00,
            0x00, 0x14, 0xA8,
            0x2C, 0x16, 0x08,
            0x39, 0xA7, 0x8E
    };
    private int height = -1, width = -1;

    //    private int mFillColor;
//    private int mAccentColor;
//    private int mHighlightColor;
//    private int mBaseColor;
    private float pc = 0f; // percent, set to 0.01f * height, all units are based on percent
    private float mCenterX, mCenterY;
    private Paint mFillPaint;
    private Paint mAccentPaint;
    private Paint mHighlightPaint;
    private Paint mBasePaint;
    private Paint mAmbientPaint;
    private Paint mShadowPaint;
    private GradientPaint mFillHighlightPaint = new GradientPaint();
    private GradientPaint mAccentFillPaint = new GradientPaint(),
            mBezelPaint1,
            mBezelPaint2 = new GradientPaint();
    private GradientPaint mAccentHighlightPaint = new GradientPaint();
    private GradientPaint mBaseAccentPaint = new GradientPaint();
    private WatchFacePreset mWatchFacePreset;
    private Settings mSettings;
    private int mPreviousSerial = -1;
    private Context mContext;
    private long[] wikipediaColors = new long[wikipediaNames.length];

    public PaintBox(Context context, WatchFacePreset watchFacePreset, Settings settings) {
        mWatchFacePreset = watchFacePreset;
        mSettings = settings;
        mContext = context;
        mFillPaint = newDefaultPaint();
        mAccentPaint = newDefaultPaint();
        mHighlightPaint = newDefaultPaint();
        mBasePaint = newDefaultPaint();

//        mFillHighlightPaint = newDefaultPaint();
//        mAccentFillPaint = newDefaultPaint();
//        mBezelPaint2 = newDefaultPaint();
//        mAccentHighlightPaint = newDefaultPaint();
//        mBaseAccentPaint = newDefaultPaint();

        mAmbientPaint = newDefaultPaint();
        mAmbientPaint.setStyle(Paint.Style.STROKE);
        mAmbientPaint.setColor(Color.WHITE); // Ambient is always white, we'll tint it in post.
//        mAmbientPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mBaseColor);

        mShadowPaint = newDefaultPaint();
        mShadowPaint.setStyle(Paint.Style.FILL);
//        mShadowPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
//        mShadowPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, Color.WHITE);

//        generatePalette();
//        generateHugeListOfColors();
//        generateTuples();
    }

    private static Paint newDefaultPaint() {
        Paint result = new Paint();
        result.setStrokeJoin(Paint.Join.ROUND);
        result.setStrokeCap(Paint.Cap.ROUND);
        result.setAntiAlias(true);
        result.setTextAlign(Paint.Align.CENTER);
        return result;
    }

//    public Paint getAccentHighlightPaint() {
//        regeneratePaints2();
//        return mAccentHighlightPaint;
//    }
//
//    public Paint getBaseAccentPaint() {
//        regeneratePaints2();
//        return mBaseAccentPaint;
//    }

//    public Paint getTickPaint() {
//        regeneratePaints2();
//        return mTickPaint;
//    }

//    public Paint getBackgroundPaint() {
//        regeneratePaints2();
//        return mBackgroundPaint;
//    }

    //    private Paint mHandPaint;
//    private Paint mTickPaint;
//    private Paint mBackgroundPaint;

    private static void generatePalette() {
        if (Build.VERSION.SDK_INT >= 26) {
            int[] i = new int[]{255, 170, 85, 0};
            String[] s = new String[]{"FF", "AA", "55", "00"};
            ColorSpace CIE_LAB = ColorSpace.get(ColorSpace.Named.CIE_LAB);
//            ColorSpace sRGB = ColorSpace.get(ColorSpace.Named.SRGB);
//            ColorSpace.Connector connector =
//                    ColorSpace.connect(sRGB, CIE_LAB, ColorSpace.RenderIntent.PERCEPTUAL);

            StringBuilder sb = new StringBuilder();
            for (int r = 0; r < i.length; r++) {
                for (int g = 0; g < i.length; g++) {
                    for (int b = 0; b < i.length; b++) {
                        long lab = Color.convert(Color.argb(0, i[r], i[g], i[b]), CIE_LAB);

//                        sb.append(String.format("(%d, %d, %d) → (%d, %d, %d)",
//                                r, g, b,
//                                (int)Color.red(lab), (int)Color.green(lab), (int)Color.blue(lab)
//                                ));

                        sb.append(String.format("{\"group\": \"#%s%s%s\", \"x\": %d, \"y\": %d, \"z\": %d},",
                                s[r], s[g], s[b], (int) Color.red(lab), (int) Color.green(lab), (int) Color.blue(lab)
                        ));
                        sb.append(System.lineSeparator());
                    }
                }
                android.util.Log.d("AnalogWatchFace", sb.toString());
                sb.setLength(0);
            }
        }
    }

    private static void generatePalette1() {
        if (Build.VERSION.SDK_INT >= 26) {
            float[] ls = new float[]{100f, 75f, 50f, 25f, 0f};
//            float [] as = new float[] {-128f, -64f, 0f, 64f, 128f};
//            float [] bs = new float[] {-128f, -64f, 0f, 64f, 128f};
            float[] bs = new float[]{128f, 96f, 64f, 32f, 0f, -32f, -64f, -96f, -128f};
            float[] as = new float[]{-128f, -96f, -64f, -32f, 0f, 32f, 64f, 96f, 128f};

            ColorSpace CIE_LAB = ColorSpace.get(ColorSpace.Named.CIE_LAB);
            ColorSpace.Connector connector =
                    ColorSpace.connect(CIE_LAB, ColorSpace.RenderIntent.PERCEPTUAL);

            for (int lc = 0; lc < ls.length; lc++) {
                StringBuilder sb = new StringBuilder();
                for (int bc = 0; bc < bs.length; bc++) {
                    sb.append("<div>").append(System.lineSeparator()).append("    ");
                    for (int ac = 0; ac < as.length; ac++) {
                        float[] f = connector.transform(ls[lc], as[ac], bs[bc]);
                        int r = Color.toArgb(Color.pack(f[0], f[1], f[2]));
//                        Color c = Color.valueOf(ls[lc], as[ac], bs[bc], 0f, CIE_LAB);
//                        int r = Color.toArgb(Color.convert(c.pack(), connector));
                        sb.append("<div class=\"A\" style=\"background-color: ");
                        sb.append(String.format("#%06X", (0xFFFFFF & r)));
                        sb.append("\"></div>");
                    }
                    sb.append("</div>").append(System.lineSeparator());

                    android.util.Log.d("AnalogWatchFace", sb.toString());
                    sb.setLength(0);
                }
                sb.append("<div><hr></div>").append(System.lineSeparator());
                android.util.Log.d("AnalogWatchFace", sb.toString());
            }
        }
    }

    /**
     * Given two colors A and B, return an intermediate color between the two. The distance
     * between the two is given by "d"; 1.0 means return "colorA", 0.0 means return "colorB",
     * 0.5 means return something evenly between the two.
     * <p>
     * For SDK 26 (Android O) and above, the calculation is done in the LAB color space for
     * extra perceptual accuracy!
     *
     * @param colorA One color to calculate
     * @param colorB The other color
     * @param d      The distance from colorB, between 0.0 and 1.0
     * @return A color between colorA and colorB
     */
    @ColorInt
    static int getIntermediateColor(@ColorInt int colorA, @ColorInt int colorB, double d) {
        // Clamp to [0, 1]
        if (d < 0) d = 0;
        else if (d > 1) d = 1;
        double e = 1d - d;

        // The "long colors" feature is only available in SDK 26 onwards!
        if (Build.VERSION.SDK_INT >= 26) {
            ColorSpace CIE_LAB = ColorSpace.get(ColorSpace.Named.CIE_LAB);
            ColorSpace sRGB = ColorSpace.get(ColorSpace.Named.SRGB);

            // Convert colors to LAB color space.
            long colorAL = Color.convert(colorA, CIE_LAB);
            long colorBL = Color.convert(colorB, CIE_LAB);

            // Generate a new color that is between the two.
            float a = (float) (Color.alpha(colorAL) * d + Color.alpha(colorBL) * e);
            float r = (float) (Color.red(colorAL) * d + Color.red(colorBL) * e);
            float g = (float) (Color.green(colorAL) * d + Color.green(colorBL) * e);
            float b = (float) (Color.blue(colorAL) * d + Color.blue(colorBL) * e);

            // Convert back to sRGB and return.
            return Color.toArgb(Color.convert(r, g, b, a, CIE_LAB, sRGB));
        } else {
            // Generate a new color that is between the two.
            int a = (int) (Color.alpha(colorA) * d + Color.alpha(colorB) * e);
            int r = (int) (Color.red(colorA) * d + Color.red(colorB) * e);
            int g = (int) (Color.green(colorA) * d + Color.green(colorB) * e);
            int b = (int) (Color.blue(colorA) * d + Color.blue(colorB) * e);

            // And return
            return Color.argb(a, r, g, b);
        }
    }

//    public void setPalette(WatchFacePreset mWatchFacePreset) {
//        this.mFillColor = mWatchFacePreset.getColor(WatchFacePreset.ColorType.FILL);
//        this.mAccentColor = mWatchFacePreset.getColor(WatchFacePreset.ColorType.ACCENT);
//        this.mHighlightColor = mWatchFacePreset.getColor(WatchFacePreset.ColorType.HIGHLIGHT);
//        this.mBaseColor = mWatchFacePreset.getColor(WatchFacePreset.ColorType.BASE);
//
//        regeneratePaints();
//    }

    public Paint getAmbientPaint() {
        regeneratePaints2();
        return mAmbientPaint;
    }

//    private void regeneratePaints() {
//        if (width <= 0 || height <= 0)
//            return;
//
//        mFillPaint.setColor(mFillColor);
//        mAccentPaint.setColor(mAccentColor);
//        mHighlightPaint.setColor(mHighlightColor);
//        mBasePaint.setColor(mBaseColor);
//
//        mFillHighlightPaint.setColors(mFillColor, mHighlightColor, WatchFacePreset.GradientStyle.RADIAL_BRUSHED);
//        mAccentFillPaint.setColors(mAccentColor, mFillColor, WatchFacePreset.GradientStyle.SWEEP);
//        mBezelPaint2.setColors(mFillColor, mAccentColor, WatchFacePreset.GradientStyle.SWEEP);
//        mAccentHighlightPaint.setColors(mAccentColor, mHighlightColor, WatchFacePreset.GradientStyle.SWEEP);
//        mBaseAccentPaint.setColors(mBaseColor, mAccentColor, WatchFacePreset.GradientStyle.SWEEP_BRUSHED);
//
//        mTickPaint = mAccentHighlightPaint;
//        mBackgroundPaint = mBaseAccentPaint;
//        // TODO: make the above only trigger when the colors actually change.
//        // TODO: actually, just hook it up to the WatchFacePreset code...
//    }

    public Paint getShadowPaint() {
        regeneratePaints2();
        return mShadowPaint;
    }

    public Paint getFillHighlightPaint() {
        regeneratePaints2();
        return mFillHighlightPaint;
    }

    public Paint getBezelPaint1() {
        regeneratePaints2();
        return mBezelPaint1;
    }

    public Paint getBezelPaint2() {
        regeneratePaints2();
        return mBezelPaint2;
    }

    public void onWidthAndHeightChanged(int width, int height) {
        if (this.width == width && this.height == height) {
            return;
        }

        this.width = width;
        this.height = height;
        pc = 0.01f * Math.min(height, width);
        mCenterX = width / 2f;
        mCenterY = height / 2f;

//            /*
//             * Calculate lengths of different hands based on watch screen size.
//             */
//            mSecondHandLength = 43.75f * pc; // 43.75%
//            mMinuteHandLength = 37.5f * pc; // 37.5%
//            mHourHandLength = 25f * pc; // 25%
//            // I changed my mind...
//            mSecondHandLength = 45f * pc; // 45%
//            mMinuteHandLength = 40f * pc; // 40%
//            mHourHandLength = 30f * pc; // 30%

//        // Regenerate stroke widths based on value of "percent"
//        mFillHighlightPaint.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT * pc);
//        mAccentFillPaint.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT * pc);
//        mBezelPaint2.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT * pc);
//        mAccentHighlightPaint.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT * pc);
//        mBaseAccentPaint.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT * pc);
//        mAmbientPaint.setStrokeWidth(AMBIENT_PAINT_STROKE_WIDTH_PERCENT * pc);
//
//        regeneratePaints();
    }

    /**
     * Get the given color from our 6-bit (64-color) palette. Returns a ColorInt.
     *
     * @param sixBitColor Index of the color from the palette, between 0 and 63
     * @return Color from our palette as a ColorInt
     */
    @ColorInt
    public int getColor(int sixBitColor) {
        return mContext.getResources().getIntArray(R.array.six_bit_colors)[sixBitColor];
    }

    /**
     * Get the given color from our 6-bit (64-color) palette. Returns a ColorInt.
     *
     * @param colorType ColorType to get from our current WatchFacePreset.
     * @return Color from our palette as a ColorInt
     */
    @ColorInt
    public int getColor(ColorType colorType) {
        switch (colorType) {
            case FILL: {
                return getColor(mWatchFacePreset.getFillSixBitColor());
            }
            case ACCENT: {
                return getColor(mWatchFacePreset.getAccentSixBitColor());
            }
            case HIGHLIGHT: {
                return getColor(mWatchFacePreset.getHighlightSixBitColor());
            }
            case BASE: {
                return getColor(mWatchFacePreset.getBaseSixBitColor());
            }
            case AMBIENT_DAY: {
                return getColor(mSettings.getAmbientDaySixBitColor());
            }
            default:
            case AMBIENT_NIGHT: {
                return getColor(mSettings.getAmbientNightSixBitColor());
            }
        }
    }

    public void setSixBitColor(ColorType colorType, @ColorInt int sixBitColor) {
        switch (colorType) {
            case FILL: {
                mWatchFacePreset.setFillSixBitColor(sixBitColor);
                break;
            }
            case ACCENT: {
                mWatchFacePreset.setAccentSixBitColor(sixBitColor);
                break;
            }
            case HIGHLIGHT: {
                mWatchFacePreset.setHighlightSixBitColor(sixBitColor);
                break;
            }
            case BASE: {
                mWatchFacePreset.setBaseSixBitColor(sixBitColor);
                break;
            }
            case AMBIENT_DAY: {
                mSettings.setAmbientDaySixBitColor(sixBitColor);
                break;
            }
            case AMBIENT_NIGHT: {
                mSettings.setAmbientNightSixBitColor(sixBitColor);
                break;
            }
            default: {
                break;
            }
        }
    }

    /**
     * Get the name of the given color from our 6-bit (64-color) palette. Returns a ColorInt.
     *
     * @param sixBitColor Index of the color from the palette, between 0 and 63
     * @return Name of the color from our palette as a ColorInt
     */
    public String getColorName(int sixBitColor) {
        return mContext.getResources().getStringArray(R.array.six_bit_color_names)[sixBitColor];
    }

    private void regeneratePaints2() {
        // Invalidate if any of our colors or styles have changed.
        int currentSerial = Objects.hash(
                getColor(ColorType.FILL),
                getColor(ColorType.ACCENT),
                getColor(ColorType.HIGHLIGHT),
                getColor(ColorType.BASE),
                getColor(ColorType.AMBIENT_DAY),
                getColor(ColorType.AMBIENT_NIGHT),
                mWatchFacePreset.getFillHighlightStyle(),
                mWatchFacePreset.getAccentFillStyle(),
                mWatchFacePreset.getAccentHighlightStyle(),
                mWatchFacePreset.getBaseAccentStyle(),
                pc);
        if (mPreviousSerial == currentSerial || width <= 0 || height <= 0) {
            return;
        }

        mPreviousSerial = currentSerial;

        mFillPaint.setColor(getColor(ColorType.FILL));
        mAccentPaint.setColor(getColor(ColorType.ACCENT));
        mHighlightPaint.setColor(getColor(ColorType.HIGHLIGHT));
        mBasePaint.setColor(getColor(ColorType.BASE));

        mFillHighlightPaint.setColors(
                ColorType.FILL, ColorType.HIGHLIGHT, mWatchFacePreset.getFillHighlightStyle());
        mAccentFillPaint.setColors(
                ColorType.ACCENT, ColorType.FILL, mWatchFacePreset.getAccentFillStyle());
        mBezelPaint1 = mAccentFillPaint;
        mBezelPaint2.setColors(
                ColorType.FILL, ColorType.ACCENT, mWatchFacePreset.getAccentFillStyle());
        mAccentHighlightPaint.setColors(
                ColorType.ACCENT, ColorType.HIGHLIGHT, mWatchFacePreset.getAccentHighlightStyle());
        mBaseAccentPaint.setColors(
                ColorType.BASE, ColorType.ACCENT, mWatchFacePreset.getBaseAccentStyle());

        mShadowPaint.setColor(getColor(ColorType.BASE));

        // Regenerate stroke widths based on value of "percent"
        mFillHighlightPaint.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT * pc);
        mAccentFillPaint.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT * pc);
        mBezelPaint2.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT * pc);
        mAccentHighlightPaint.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT * pc);
        mBaseAccentPaint.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT * pc);
        mAmbientPaint.setStrokeWidth(AMBIENT_PAINT_STROKE_WIDTH_PERCENT * pc);
    }

    private Tuple generateMidPoint(Tuple a1, Tuple b1, float d) {
        float e = 1f - d;
        float a = (a1.a * e) + (b1.a * d);
        float b = (a1.b * e) + (b1.b * d);
        float c = (a1.c * e) + (b1.c * d);
        float x = (a1.x * e) + (b1.x * d);
        float y = (a1.y * e) + (b1.y * d);
        float z = (a1.z * e) + (b1.z * d);
        return new Tuple(Math.round(a), Math.round(b), Math.round(c), x, y, z);
    }

    private Tuple generateMidPoint(Tuple a1, Tuple b1, float d1, Tuple a2, Tuple b2, float d2) {
        Tuple t1 = generateMidPoint(a1, b1, d1);
        Tuple t2 = generateMidPoint(a2, b2, d2);
        if (t1.a != t2.a || t2.b != t2.b || t1.c != t2.c) {
            android.util.Log.d("AnalogWatchFace", String.format("Error: (%d, %d, %d) != (%d, %d, %d)", t1.a, t1.b, t1.c, t2.a, t2.b, t2.c));
        }
        return generateMidPoint(t1, t2, 0.5f);
    }

    private Tuple generateMidPoint(Tuple a1, Tuple b1, float d1, Tuple a2, Tuple b2, float d2, Tuple a3, Tuple b3, float d3) {
        Tuple t1 = generateMidPoint(a1, b1, d1);
        Tuple t2 = generateMidPoint(a2, b2, d2);
        Tuple t3 = generateMidPoint(a3, b3, d3);
        return new Tuple(t1.a, t1.b, t1.c,
                (t1.x + t2.x + t3.x) / 3f,
                (t1.y + t2.y + t3.y) / 3f,
                (t1.z + t2.z + t3.z) / 3f);
    }

    private void generateTuples() {
        if (Build.VERSION.SDK_INT < 26) return;

        Tuple t333 = new Tuple(3, 3, 3).log();
        Tuple t330 = new Tuple(3, 3, 0).log();
        Tuple t303 = new Tuple(3, 0, 3).log();
        Tuple t300 = new Tuple(3, 0, 0).log();
        Tuple t033 = new Tuple(0, 3, 3).log();
        Tuple t030 = new Tuple(0, 3, 0).log();
        Tuple t003 = new Tuple(0, 0, 3).log();
        Tuple t000 = new Tuple(0, 0, 0).log();

        // Edges
        Tuple t331 = generateMidPoint(t330, t333, 0.333333f).log();
        Tuple t332 = generateMidPoint(t330, t333, 0.666667f).log();
        Tuple t313 = generateMidPoint(t303, t333, 0.333333f).log();
        Tuple t323 = generateMidPoint(t303, t333, 0.666667f).log();
        Tuple t301 = generateMidPoint(t300, t303, 0.333333f).log();
        Tuple t302 = generateMidPoint(t300, t303, 0.666667f).log();
        Tuple t310 = generateMidPoint(t300, t330, 0.333333f).log();
        Tuple t320 = generateMidPoint(t300, t330, 0.666667f).log();

        Tuple t031 = generateMidPoint(t030, t033, 0.333333f).log();
        Tuple t032 = generateMidPoint(t030, t033, 0.666667f).log();
        Tuple t013 = generateMidPoint(t003, t033, 0.333333f).log();
        Tuple t023 = generateMidPoint(t003, t033, 0.666667f).log();
        Tuple t001 = generateMidPoint(t000, t003, 0.333333f).log();
        Tuple t002 = generateMidPoint(t000, t003, 0.666667f).log();
        Tuple t010 = generateMidPoint(t000, t030, 0.333333f).log();
        Tuple t020 = generateMidPoint(t000, t030, 0.666667f).log();

        Tuple t133 = generateMidPoint(t033, t333, 0.333333f).log();
        Tuple t130 = generateMidPoint(t030, t330, 0.333333f).log();
        Tuple t103 = generateMidPoint(t003, t303, 0.333333f).log();
        Tuple t100 = generateMidPoint(t000, t300, 0.333333f).log();
        Tuple t233 = generateMidPoint(t033, t333, 0.666667f).log();
        Tuple t230 = generateMidPoint(t030, t330, 0.666667f).log();
        Tuple t203 = generateMidPoint(t003, t303, 0.666667f).log();
        Tuple t200 = generateMidPoint(t000, t300, 0.666667f).log();

        // Faces
        Tuple t311 = generateMidPoint(t310, t313, 0.333333f, t301, t331, 0.333333f).log();
        Tuple t312 = generateMidPoint(t310, t313, 0.666667f, t302, t332, 0.333333f).log();
        Tuple t321 = generateMidPoint(t320, t323, 0.333333f, t301, t331, 0.666667f).log();
        Tuple t322 = generateMidPoint(t320, t323, 0.666667f, t302, t332, 0.666667f).log();

        Tuple t011 = generateMidPoint(t010, t013, 0.333333f, t001, t031, 0.333333f).log();
        Tuple t012 = generateMidPoint(t010, t013, 0.666667f, t002, t032, 0.333333f).log();
        Tuple t021 = generateMidPoint(t020, t023, 0.333333f, t001, t031, 0.666667f).log();
        Tuple t022 = generateMidPoint(t020, t023, 0.666667f, t002, t032, 0.666667f).log();

        Tuple t101 = generateMidPoint(t100, t103, 0.333333f, t001, t301, 0.333333f).log();
        Tuple t102 = generateMidPoint(t100, t103, 0.666667f, t002, t302, 0.333333f).log();
        Tuple t201 = generateMidPoint(t200, t203, 0.333333f, t001, t301, 0.666667f).log();
        Tuple t202 = generateMidPoint(t200, t203, 0.666667f, t002, t302, 0.666667f).log();

        Tuple t131 = generateMidPoint(t130, t133, 0.333333f, t031, t331, 0.333333f).log();
        Tuple t132 = generateMidPoint(t130, t133, 0.666667f, t032, t332, 0.333333f).log();
        Tuple t231 = generateMidPoint(t230, t233, 0.333333f, t031, t331, 0.666667f).log();
        Tuple t232 = generateMidPoint(t230, t233, 0.666667f, t032, t332, 0.666667f).log();

        Tuple t110 = generateMidPoint(t100, t130, 0.333333f, t010, t310, 0.333333f).log();
        Tuple t120 = generateMidPoint(t100, t130, 0.666667f, t020, t320, 0.333333f).log();
        Tuple t210 = generateMidPoint(t200, t230, 0.333333f, t010, t310, 0.666667f).log();
        Tuple t220 = generateMidPoint(t200, t230, 0.666667f, t020, t320, 0.666667f).log();

        Tuple t113 = generateMidPoint(t103, t133, 0.333333f, t013, t313, 0.333333f).log();
        Tuple t123 = generateMidPoint(t103, t133, 0.666667f, t023, t323, 0.333333f).log();
        Tuple t213 = generateMidPoint(t203, t233, 0.333333f, t013, t313, 0.666667f).log();
        Tuple t223 = generateMidPoint(t203, t233, 0.666667f, t023, t323, 0.666667f).log();

        // Inside
        /*Tuple t111 =*/
        generateMidPoint(t110, t113, 0.333333f, t101, t131, 0.333333f, t011, t311, 0.333333f).log();
        /*Tuple t112 =*/
        generateMidPoint(t110, t113, 0.666667f, t102, t132, 0.333333f, t012, t312, 0.333333f).log();
        /*Tuple t121 =*/
        generateMidPoint(t120, t123, 0.333333f, t101, t131, 0.666667f, t021, t321, 0.333333f).log();
        /*Tuple t122 =*/
        generateMidPoint(t120, t123, 0.666667f, t102, t132, 0.666667f, t022, t322, 0.333333f).log();
        /*Tuple t211 =*/
        generateMidPoint(t210, t213, 0.333333f, t201, t231, 0.333333f, t011, t311, 0.666667f).log();
        /*Tuple t212 =*/
        generateMidPoint(t210, t213, 0.666667f, t202, t232, 0.333333f, t012, t312, 0.666667f).log();
        /*Tuple t221 =*/
        generateMidPoint(t220, t223, 0.333333f, t201, t231, 0.666667f, t021, t321, 0.666667f).log();
        /*Tuple t222 =*/
        generateMidPoint(t220, t223, 0.666667f, t202, t232, 0.666667f, t022, t322, 0.666667f).log();
    }

    public Paint getPaintFromPreset(WatchFacePreset.Style style) {
        regeneratePaints2();
        switch (style) {
            case FILL:
                return mFillPaint;
            case ACCENT:
                return mAccentPaint;
            case HIGHLIGHT:
                return mHighlightPaint;
            case BASE:
                return mBasePaint;
            case FILL_HIGHLIGHT:
                return mFillHighlightPaint;
            case ACCENT_FILL:
                return mAccentFillPaint;
            case ACCENT_HIGHLIGHT:
                return mAccentHighlightPaint;
            case ACCENT_BASE:
                return mBaseAccentPaint;
            default:
                // Should never hit this.
                return mFillPaint;
        }
    }

    @TargetApi(26)
    private void generateHugeListOfColors() {
        ColorSpace CIE_LAB = ColorSpace.get(ColorSpace.Named.CIE_LAB);

        Log.d("AnalogWatchFace", "Generating lots of colors...");

        for (int i = 0; i < wikipediaNames.length; i++) {
            wikipediaColors[i] = Color.convert(Color.argb(255,
                    wikipediaRawColors[i * 3 + 0],
                    wikipediaRawColors[i * 3 + 1],
                    wikipediaRawColors[i * 3 + 2]), CIE_LAB);
        }

        Log.d("AnalogWatchFace", "Generated!");
    }

    private static SparseArray<WeakReference<Bitmap>> mBitmapCache = new SparseArray<>();

    private static Bitmap mTempBitmap;
    private static Canvas mTempCanvas;

    public enum ColorType {FILL, ACCENT, HIGHLIGHT, BASE, AMBIENT_DAY, AMBIENT_NIGHT}

    private class GradientPaint extends Paint {
        private int mCustomHashCode = -1;

        Paint mBrushedEffectPaint = new Paint();
        Path mBrushedEffectPath = new Path();

        GradientPaint() {
            super();

            // From "newDefaultPaint".
            this.setStrokeJoin(Paint.Join.ROUND);
            this.setStrokeCap(Paint.Cap.ROUND);
            this.setAntiAlias(true);
            this.setTextAlign(Paint.Align.CENTER);
        }

        private void addSweepGradient(int colorA, int colorB) {
//        paint.setShader(new SweepGradient(mCenterX, mCenterY,
//                new int[]{colorA, colorB, colorA, colorB, colorA},
//                null));
            int[] gradient = new int[]{
                    getIntermediateColor(colorA, colorB, 1.0d), // Original
                    getIntermediateColor(colorA, colorB, 0.8d),
                    getIntermediateColor(colorA, colorB, 0.6d),
                    getIntermediateColor(colorA, colorB, 0.4d),
                    getIntermediateColor(colorA, colorB, 0.2d),
                    getIntermediateColor(colorA, colorB, 0.0d), // Original
                    getIntermediateColor(colorA, colorB, 0.2d),
                    getIntermediateColor(colorA, colorB, 0.4d),
                    getIntermediateColor(colorA, colorB, 0.6d),
                    getIntermediateColor(colorA, colorB, 0.8d),
                    getIntermediateColor(colorA, colorB, 1.0d), // Original
                    getIntermediateColor(colorA, colorB, 0.8d),
                    getIntermediateColor(colorA, colorB, 0.6d),
                    getIntermediateColor(colorA, colorB, 0.4d),
                    getIntermediateColor(colorA, colorB, 0.2d),
                    getIntermediateColor(colorA, colorB, 0.0d), // Original
                    getIntermediateColor(colorA, colorB, 0.2d),
                    getIntermediateColor(colorA, colorB, 0.4d),
                    getIntermediateColor(colorA, colorB, 0.6d),
                    getIntermediateColor(colorA, colorB, 0.8d),
                    getIntermediateColor(colorA, colorB, 1.0d), // Original
            };
            setShader(new SweepGradient(mCenterX, mCenterY, gradient, null));
        }

        private void addRadialGradient(int colorA, int colorB) {
//        paint.setShader(new RadialGradient(mCenterX, mCenterY, mCenterY,
//                new int[]{colorB, colorB, colorB, colorA, colorA},
//                null, Shader.TileMode.CLAMP));

            int[] gradient = new int[]{
                    colorB, // Original
                    colorB,
                    colorB,
                    colorB,
                    colorB,
                    colorB, // Original
                    colorB,
                    colorB,
                    colorB,
                    colorB,
                    colorB, // Original
                    getIntermediateColor(colorA, colorB, 0.2d),
                    getIntermediateColor(colorA, colorB, 0.4d),
                    getIntermediateColor(colorA, colorB, 0.6d),
                    getIntermediateColor(colorA, colorB, 0.8d),
                    colorA, // Original
                    colorA,
                    colorA,
                    colorA,
                    colorA,
                    colorA // Original
            };
            setShader(new RadialGradient(mCenterX, mCenterY, mCenterY, gradient, null,
                    Shader.TileMode.CLAMP));
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), mCustomHashCode);
        }

        void setColors(ColorType colorTypeA,
                       ColorType colorTypeB,
                       WatchFacePreset.GradientStyle gradientStyle) {
            @ColorInt int colorA = PaintBox.this.getColor(colorTypeA);
            @ColorInt int colorB = PaintBox.this.getColor(colorTypeB);

            mCustomHashCode = Objects.hash(colorA, colorB, gradientStyle, height, width);

            switch (gradientStyle) {
                case SWEEP:
                    addSweepGradient(colorA, colorB);
                    break;
                case RADIAL:
                    addRadialGradient(colorA, colorB);
                    break;
                case SWEEP_BRUSHED:
                    addSweepGradient(colorA, colorB);
                    addBrushedEffect();
                    break;
                case RADIAL_BRUSHED:
                    addRadialGradient(colorA, colorB);
                    addBrushedEffect();
                    break;
                default:
                    // Should never hit this.
                    break;
            }
        }

        private void addBrushedEffect() {
            Bitmap brushedEffectBitmap = generateWeaveEffect();

            setShader(new BitmapShader(brushedEffectBitmap,
                    Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        }

        private Bitmap generateBrushedEffect() {
            // Attempt to return an existing bitmap from the cache if we have one.
            WeakReference<Bitmap> cache = mBitmapCache.get(mCustomHashCode);
            if (cache != null) {
                // Well, we have an existing bitmap, but it may have been garbage collected...
                Bitmap result = cache.get();
                if (result != null) {
                    // It wasn't garbage collected! Return it.
                    return result;
                }
            }

            // Generate a new bitmap.
            Bitmap brushedEffectBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas brushedEffectCanvas = new Canvas(brushedEffectBitmap);

            // Cache it for next time's use.
            mBitmapCache.put(mCustomHashCode, new WeakReference<>(brushedEffectBitmap));

            float percent = mCenterX / 50f;
            float offset = 0.5f * percent;
            int alpha = 50;
            float mCenter = Math.min(mCenterX, mCenterY);

            mBrushedEffectPaint.setStyle(Style.STROKE);
            mBrushedEffectPaint.setStrokeWidth(offset);
            mBrushedEffectPaint.setStrokeJoin(Join.ROUND);
            mBrushedEffectPaint.setAntiAlias(true);

//            brushedEffectCanvas.drawPaint(this);

            // Spun metal circles?
            for (float max = 50f, i = max; i > 0f; i--) {
                mBrushedEffectPath.reset();
                mBrushedEffectPath.addCircle(mCenterX, mCenterY, mCenter * i / max, Path.Direction.CW);

                mBrushedEffectPath.offset(-offset, -offset);
                mBrushedEffectPaint.setColor(Color.WHITE);
                mBrushedEffectPaint.setAlpha(alpha);
                brushedEffectCanvas.drawPath(mBrushedEffectPath, mBrushedEffectPaint);

                mBrushedEffectPath.offset(2f * offset, 2f * offset);
                mBrushedEffectPaint.setColor(Color.BLACK);
                mBrushedEffectPaint.setAlpha(alpha);
                brushedEffectCanvas.drawPath(mBrushedEffectPath, mBrushedEffectPaint);

                mBrushedEffectPath.offset(-offset, -offset);
                brushedEffectCanvas.drawPath(mBrushedEffectPath, this);
            }
            return brushedEffectBitmap;
        }


        private Bitmap generateWeaveEffect() {
            // Attempt to return an existing bitmap from the cache if we have one.
            WeakReference<Bitmap> cache = mBitmapCache.get(mCustomHashCode);
            if (cache != null) {
                // Well, we have an existing bitmap, but it may have been garbage collected...
                Bitmap result = cache.get();
                if (result != null) {
                    // It wasn't garbage collected! Return it.
                    return result;
                }
            }

            // Generate a new bitmap.
            Bitmap brushedEffectBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas brushedEffectCanvas = new Canvas(brushedEffectBitmap);

            // Cache it for next time's use.
            mBitmapCache.put(mCustomHashCode, new WeakReference<>(brushedEffectBitmap));

            // Temp bitmap?
            if (mTempBitmap == null ||
                    (mTempBitmap.getWidth() != width && mTempBitmap.getHeight() != height)) {
                mTempBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                mTempCanvas = new Canvas(mTempBitmap);
            } else {
                // Zero out the canvas.
                mTempCanvas.drawColor(Color.TRANSPARENT);
            }

            float percent = mCenterX / 50f;
            float offset = 0.25f * percent;
            float alphaMax = 50f;
            float alphaExtra = 40f;
//            float mCenter = Math.min(mCenterX, mCenterY);

            Shader vignette = new RadialGradient(
                    mCenterX, mCenterY, mCenterY,
                    new int[]{Color.BLACK, Color.BLACK, Color.TRANSPARENT},
                    new float[]{0f, 0.85f, 0.95f}, Shader.TileMode.CLAMP);

            Xfermode gradientTransferMode = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);

            Paint gradientH = new Paint();
            gradientH.setShader(new ComposeShader(
                    vignette,
                    new LinearGradient(
                            width * 0.3f, 0f, width * 0.7f, height,
                            new int[]{Color.TRANSPARENT, Color.BLACK, Color.TRANSPARENT},
                            new float[]{0.3f, 0.5f, 0.7f}, Shader.TileMode.CLAMP),
                    PorterDuff.Mode.SRC_IN));
            gradientH.setXfermode(gradientTransferMode);

            Paint gradientV = new Paint();
            gradientV.setShader(new ComposeShader(
                    vignette,
                    new LinearGradient(
                            0f, height * 0.7f, width, height * 0.3f,
                            new int[]{Color.TRANSPARENT, Color.BLACK, Color.TRANSPARENT},
                            new float[]{0.3f, 0.5f, 0.7f}, Shader.TileMode.CLAMP),
                    PorterDuff.Mode.SRC_IN));
            gradientV.setXfermode(gradientTransferMode);

            mBrushedEffectPaint.setStyle(Style.STROKE);
            mBrushedEffectPaint.setStrokeWidth(offset);
            mBrushedEffectPaint.setStrokeJoin(Join.ROUND);
            mBrushedEffectPaint.setAntiAlias(true);

            brushedEffectCanvas.drawPaint(this);

            int prevAlpha = getAlpha();

            int weaves = 10, fibres = 5;

            // Horizontal
            for (int i = 0; i < weaves; i += 1) {
                float heightI = height / (float) weaves;
                float center = ((float) i + 0.5f) * heightI;

                for (int j = fibres * 2 - 1; j > 0; j -= 2) {
                    // Height = 100
                    // Fibres = 5;

                    // j = 9, 7, 5, 3, 1
                    // Heights = 100, 77, 55, 33, 11
                    float weightJ = (float) j / ((float) fibres * 2f - 1f);
                    float heightJ = weightJ * heightI;
                    int alpha = (int) (alphaMax - alphaExtra * weightJ);

                    float h = heightJ / 2f;
                    mBrushedEffectPaint.setStyle(Style.STROKE);

                    mBrushedEffectPath.reset();
                    mBrushedEffectPath.addRect(
                            0, center - h, width, center + h, Path.Direction.CW);

                    mBrushedEffectPath.offset(-offset, -offset);
                    mBrushedEffectPaint.setColor(Color.WHITE);
                    mBrushedEffectPaint.setAlpha(alpha);
                    mTempCanvas.drawPath(mBrushedEffectPath, mBrushedEffectPaint);

                    mBrushedEffectPath.offset(2f * offset, 2f * offset);
                    mBrushedEffectPaint.setColor(Color.BLACK);
                    mBrushedEffectPaint.setAlpha(alpha);
                    mTempCanvas.drawPath(mBrushedEffectPath, mBrushedEffectPaint);

                    mBrushedEffectPath.offset(-offset, -offset);
                    setAlpha(alpha);
                    mTempCanvas.drawPath(mBrushedEffectPath, this);
                }
            }

            // Apply a gradient transfer mode.
            mTempCanvas.drawPaint(gradientH);

            // Erase every 2nd square of the bitmap, and apply a transfer mode.
            Xfermode clearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
            mBrushedEffectPaint.setColor(Color.BLACK);
            mBrushedEffectPaint.setStyle(Style.FILL);
            mBrushedEffectPaint.setXfermode(clearMode);
            for (int i = 0; i < weaves; i++) {
                float heightI = height / (float) weaves;
                float centerI = ((float) i + 0.5f) * heightI;
                float top = centerI - (heightI / 2f);
                float bottom = centerI + (heightI / 2f);

                for (int j = 0; j < weaves; j++) {
                    float widthJ = width / (float) weaves;
                    float centerJ = ((float) j + 0.5f) * widthJ;
                    float left = centerJ - (widthJ / 2f);
                    float right = centerJ + (widthJ / 2f);

                    if (i % 2 == j % 2) {
//                        Log.d("Erasing", "(" + i + "," + j + ")");
                        // Only every 2nd square
                        mTempCanvas.drawRect(left, top, right, bottom, mBrushedEffectPaint);
                    }
                }
            }
            mBrushedEffectPaint.setXfermode(null);

            // OK, transfer the horizontal stripes in "mTempCanvas" to "brushedEffectCanvas".
            brushedEffectCanvas.drawBitmap(mTempBitmap, 0f, 0f, null);

            // Apply a destination atop transfer mode to only draw into transparent bits.
//            Xfermode dstMode = new PorterDuffXfermode(PorterDuff.Mode.DST_OVER);
//            mBrushedEffectPaint.setXfermode(dstMode);
//            mBrushedEffectPaint.setColor(Color.GREEN);
//            brushedEffectCanvas.drawPaint(mBrushedEffectPaint);

            // Zero out the temp canvas in preparation for next.
            mTempCanvas.drawColor(Color.TRANSPARENT);

            // Vertical
            for (int i = 0; i < weaves; i += 1) {
                float widthI = width / (float) weaves;
                float center = ((float) i + 0.5f) * widthI;

                for (int j = fibres * 2 - 1; j > 0; j -= 2) {
                    // Height = 100
                    // Fibres = 5;

                    // j = 9, 7, 5, 3, 1
                    // Heights = 100, 77, 55, 33, 11
                    float weightJ = (float) j / ((float) fibres * 2f - 1f);
                    float widthJ = weightJ * widthI;
                    int alpha = (int) (alphaMax - alphaExtra * weightJ);

                    float w = widthJ / 2f;
                    mBrushedEffectPaint.setStyle(Style.STROKE);

                    mBrushedEffectPath.reset();
                    mBrushedEffectPath.addRect(
                            center - w, 0, center + w, height, Path.Direction.CW);

                    mBrushedEffectPath.offset(-offset, -offset);
                    mBrushedEffectPaint.setColor(Color.WHITE);
                    mBrushedEffectPaint.setAlpha(alpha);
                    mTempCanvas.drawPath(mBrushedEffectPath, mBrushedEffectPaint);

                    mBrushedEffectPath.offset(2f * offset, 2f * offset);
                    mBrushedEffectPaint.setColor(Color.BLACK);
                    mBrushedEffectPaint.setAlpha(alpha);
                    mTempCanvas.drawPath(mBrushedEffectPath, mBrushedEffectPaint);

                    mBrushedEffectPath.offset(-offset, -offset);
                    setAlpha(alpha);
                    mTempCanvas.drawPath(mBrushedEffectPath, this);
                }
            }

            // Apply a gradient transfer mode.
            mTempCanvas.drawPaint(gradientV);

            // Erase every OTHER other 2nd square.
            mBrushedEffectPaint.setColor(Color.BLACK);
            mBrushedEffectPaint.setStyle(Style.FILL);
            mBrushedEffectPaint.setXfermode(clearMode);
            for (int i = 0; i < weaves; i++) {
                float heightI = height / (float) weaves;
                float centerI = ((float) i + 0.5f) * heightI;
                float top = centerI - (heightI / 2f);
                float bottom = centerI + (heightI / 2f);

                for (int j = 0; j < weaves; j++) {
                    float widthJ = width / (float) weaves;
                    float centerJ = ((float) j + 0.5f) * widthJ;
                    float left = centerJ - (widthJ / 2f);
                    float right = centerJ + (widthJ / 2f);

                    if (i % 2 != j % 2) { // Other!
                        Log.d("Erasing", "(" + i + "," + j + ")");
                        // Only every 2nd square
                        mTempCanvas.drawRect(left, top, right, bottom, mBrushedEffectPaint);
                    }
                }
            }
            mBrushedEffectPaint.setXfermode(null);

            // OK, transfer the vertical stripes in "mTempCanvas" to "brushedEffectCanvas".
            brushedEffectCanvas.drawBitmap(mTempBitmap, 0f, 0f, null);

            setAlpha(prevAlpha);

            return brushedEffectBitmap;
        }
    }

    @TargetApi(26)
    private class Tuple {
        private int a, b, c;
        private float x, y, z;

        Tuple(int a, int b, int c) {
            ColorSpace CIE_LAB = ColorSpace.get(ColorSpace.Named.CIE_LAB);
            long color = Color.convert(Color.argb(255, a * 85, b * 85, c * 85), CIE_LAB);

            this.a = a;
            this.b = b;
            this.c = c;
            this.x = Color.red(color);
            this.y = Color.green(color);
            this.z = Color.blue(color);
        }

        Tuple(int a, int b, int c, float x, float y, float z) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @NonNull
        @Override
        public String toString() {
            int i = 0;
            float best = 9999f;
            {
                for (int j = 0; j < wikipediaColors.length; j++) {
                    float x2 = Color.red(wikipediaColors[j]);
                    float y2 = Color.green(wikipediaColors[j]);
                    float z2 = Color.blue(wikipediaColors[j]);

                    float check = Math.abs(x - x2) + Math.abs(y - y2) + Math.abs(z - z2);
                    if (check < best) {
                        best = check;
                        i = j;
                    }
                }
            }
            ColorSpace CIE_LAB = ColorSpace.get(ColorSpace.Named.CIE_LAB);
            ColorSpace sRGB = ColorSpace.get(ColorSpace.Named.SRGB);
            ColorSpace.RenderIntent intent = ColorSpace.RenderIntent.PERCEPTUAL;
            ColorSpace.Connector connector = ColorSpace.connect(CIE_LAB, sRGB, intent);

            long col = Color.convert(Color.pack(x, y, z, 1.0f, CIE_LAB), connector);

            String group = String.format("0x%06X", (0xFFFFFF & Color.toArgb(col)));

            String[] hexes = new String[]{"00", "55", "AA", "FF"};
            String original = "0x" + hexes[a] + hexes[b] + hexes[c];

            return String.format("{\"a\": %d, \"b\": %d, \"c\": %d, \"group\": \"%s\", \"original\": \"%s\", \"x\": %f, \"y\": %f, \"z\": %f}, --> best match '%s' (distance %f)",
                    a, b, c, group, original, x, y, z, wikipediaNames[i], best);
        }

        public Tuple log() {
            android.util.Log.d("AnalogWatchFace", toString());
            return this;
        }
    }
}
