/*
 * Copyright 2011 Alexis Lauper <alexis.lauper@gmail.com>
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gameon.gameon.crossword.keyboard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.gameon.gameon.R;

public class KeyboardView extends LinearLayout implements OnTouchListener {

	private KeyboardViewInterface	delegate;
	private View 					currentView;
	private String 					value;
	
	/** Constructeur
	 * 
	 * @param context
	 */
	public KeyboardView(Context context) {
		super(context);
		this.initComponent();
	}
	
	/** Constructeur
	 * 
	 * @param context
	 * @param attrs
	 */
	public KeyboardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.initComponent();
	}

	/** Initialisation des composants
	 */
	public void initComponent() {
		LayoutInflater inflater = LayoutInflater.from(getContext());
		ViewGroup keyboard = (ViewGroup) inflater.inflate(R.layout.keyboard, null);
		int size = LayoutParams.FILL_PARENT;
		keyboard.setLayoutParams(new LayoutParams(size, size));
		this.addView(keyboard);

		this.findViewById(R.id.buttonAlef).setOnTouchListener(this);
		this.findViewById(R.id.buttonBet).setOnTouchListener(this);
		this.findViewById(R.id.buttonGimel).setOnTouchListener(this);
		this.findViewById(R.id.buttonDalet).setOnTouchListener(this);
		this.findViewById(R.id.buttonHei).setOnTouchListener(this);
		this.findViewById(R.id.buttonVav).setOnTouchListener(this);
		this.findViewById(R.id.buttonZain).setOnTouchListener(this);
		this.findViewById(R.id.buttonHet).setOnTouchListener(this);
		this.findViewById(R.id.buttonTet).setOnTouchListener(this);
		this.findViewById(R.id.buttonYud).setOnTouchListener(this);
		this.findViewById(R.id.buttonKaf).setOnTouchListener(this);
		this.findViewById(R.id.buttonKafSofit).setOnTouchListener(this);
		this.findViewById(R.id.buttonLamed).setOnTouchListener(this);
		this.findViewById(R.id.buttonMem).setOnTouchListener(this);
		this.findViewById(R.id.buttonMemSofit).setOnTouchListener(this);
		this.findViewById(R.id.buttonNun).setOnTouchListener(this);
		this.findViewById(R.id.buttonNunSofit).setOnTouchListener(this);
		this.findViewById(R.id.buttonSameh).setOnTouchListener(this);
		this.findViewById(R.id.buttonAyin).setOnTouchListener(this);
		this.findViewById(R.id.buttonPei).setOnTouchListener(this);
		this.findViewById(R.id.buttonPeiSofit).setOnTouchListener(this);
		this.findViewById(R.id.buttonTzadik).setOnTouchListener(this);
		this.findViewById(R.id.buttonTzadikSofit).setOnTouchListener(this);
		this.findViewById(R.id.buttonKuf).setOnTouchListener(this);
		this.findViewById(R.id.buttonResh).setOnTouchListener(this);
		this.findViewById(R.id.buttonShin).setOnTouchListener(this);
		this.findViewById(R.id.buttonTaf).setOnTouchListener(this);
		this.findViewById(R.id.buttonDELETE).setOnTouchListener(this);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
            {
            	// Get key value
        		switch (v.getId()) {
				case R.id.buttonAlef: this.value = "א"; break;
        		case R.id.buttonBet: this.value = "ב"; break;
        		case R.id.buttonGimel: this.value = "ג"; break;
        		case R.id.buttonDalet: this.value = "ד"; break;
        		case R.id.buttonHei: this.value = "ה"; break;
        		case R.id.buttonVav: this.value = "ו"; break;
        		case R.id.buttonZain: this.value = "ז"; break;
        		case R.id.buttonHet: this.value = "ח"; break;
        		case R.id.buttonTet: this.value = "ט"; break;
        		case R.id.buttonYud: this.value = "י"; break;
        		case R.id.buttonKaf: this.value = "כ"; break;
				case R.id.buttonKafSofit: this.value = "ך"; break;
        		case R.id.buttonLamed: this.value = "ל"; break;
        		case R.id.buttonMem: this.value = "מ"; break;
				case R.id.buttonMemSofit: this.value = "ם"; break;
        		case R.id.buttonNun: this.value = "נ"; break;
				case R.id.buttonNunSofit: this.value = "ן"; break;
        		case R.id.buttonSameh: this.value = "ס"; break;
        		case R.id.buttonAyin: this.value = "ע"; break;
        		case R.id.buttonPei: this.value = "פ"; break;
				case R.id.buttonPeiSofit: this.value = "ף"; break;
        		case R.id.buttonTzadik: this.value = "צ"; break;
				case R.id.buttonTzadikSofit: this.value = "ץ"; break;
        		case R.id.buttonKuf: this.value = "ק"; break;
        		case R.id.buttonResh: this.value = "ר"; break;
        		case R.id.buttonShin: this.value = "ש"; break;
				case R.id.buttonTaf: this.value = "ת"; break;
        		case R.id.buttonDELETE: this.value = null; break;
        		}

        		this.currentView = v;
        		int[] location = new int[2];
        		this.currentView.getLocationOnScreen(location);
        		
        		// Change key background (selector actually doesn't work with KeyboardView)
        		if (v.getId() == R.id.buttonDELETE)
        			this.currentView.setBackgroundResource(R.drawable.btn_keyboard_delete_pressed);
        		else
        			this.currentView.setBackgroundResource(R.drawable.btn_keyboard_pressed);
        		
            	if (this.value != null)
            		this.delegate.onKeyDown(value, location, this.currentView.getWidth());
        		break;
            }

            case MotionEvent.ACTION_UP:
            {
        		switch (v.getId()) {
        		case R.id.buttonDELETE:
            		this.delegate.onKeyUp(" ");
        			break;
        		}
        		
        		// Change key background (selector actually doesn't work with KeyboardView)
        		if (v.getId() == R.id.buttonDELETE)
        			this.currentView.setBackgroundResource(R.drawable.btn_keyboard_delete_release);
        		else
        			this.currentView.setBackgroundResource(R.drawable.btn_keyboard_release);

        		if (this.value != null)
            		this.delegate.onKeyUp(value);
        		break;
            }
        }
        // if you return false, these actions will not be recorded
        return true;
	}
	
	public void setDelegate(KeyboardViewInterface delegate) {
		this.delegate = delegate; 
}
}